package fi.dy.masa.servux.network;

import com.google.common.base.Charsets;
import fi.dy.masa.servux.mixin.IMixinCustomPayloadC2SPacket;
import fi.dy.masa.servux.network.util.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class ServerPacketChannelHandler
{
    public static final ResourceLocation REGISTER = new ResourceLocation("minecraft:register");
    public static final ResourceLocation UNREGISTER = new ResourceLocation("minecraft:unregister");

    public static final ServerPacketChannelHandler INSTANCE = new ServerPacketChannelHandler();

    private final HashMap<ResourceLocation, IPluginChannelHandler> handlers = new HashMap<>();
    private final HashMap<ResourceLocation, IPluginChannelHandler> subscribableHandlers = new HashMap<>();

    private ServerPacketChannelHandler()
    {
    }

    public void registerServerChannelHandler(IPluginChannelHandler handler)
    {
        synchronized (this.handlers)
        {
            List<ResourceLocation> toRegister = new ArrayList<>();
            ResourceLocation channel = handler.getChannel();

            if (this.handlers.containsKey(channel) == false)
            {
                this.handlers.put(channel, handler);
                toRegister.add(channel);

                if (handler.isSubscribable())
                {
                    this.subscribableHandlers.put(channel, handler);
                }
            }
        }
    }

    public void unregisterServerChannelHandler(IPluginChannelHandler handler)
    {
        synchronized (this.handlers)
        {
            List<ResourceLocation> toUnRegister = new ArrayList<>();
            ResourceLocation channel = handler.getChannel();

            if (this.handlers.remove(channel, handler))
            {
                toUnRegister.add(channel);
                this.subscribableHandlers.remove(channel);
            }
        }
    }

    public void processPacketFromClient(ServerboundCustomPayloadPacket packet, ServerGamePacketListenerImpl netHandler)
    {
        IMixinCustomPayloadC2SPacket accessor = ((IMixinCustomPayloadC2SPacket) packet);
        ResourceLocation channel = accessor.servux_getChannel();
        FriendlyByteBuf data = accessor.servux_getData();

        IPluginChannelHandler handler;

        synchronized (this.handlers)
        {
            handler = this.handlers.get(channel);
        }

        if (handler != null)
        {
            final FriendlyByteBuf slice = PacketUtils.retainedSlice(data);
            this.schedule(() -> this.handleReceivedData(channel, slice, handler, netHandler), netHandler);
        }
        else if (channel.equals(REGISTER))
        {
            final List<ResourceLocation> channels = getChannels(data);
            this.schedule(() -> this.updateRegistrationForChannels(channels, IPluginChannelHandler::subscribe, netHandler), netHandler);
        }
        else if (channel.equals(UNREGISTER))
        {
            final List<ResourceLocation> channels = getChannels(data);
            this.schedule(() -> this.updateRegistrationForChannels(channels, IPluginChannelHandler::unsubscribe, netHandler), netHandler);
        }
    }

    private void updateRegistrationForChannels(List<ResourceLocation> channels,
                                               BiConsumer<IPluginChannelHandler, ServerGamePacketListenerImpl> action,
                                               ServerGamePacketListenerImpl netHandler)
    {
        for (ResourceLocation channel : channels)
        {
            IPluginChannelHandler handler = this.subscribableHandlers.get(channel);

            if (handler != null)
            {
                action.accept(handler, netHandler);
            }
        }
    }

    private void handleReceivedData(ResourceLocation channel, FriendlyByteBuf data,
                                    IPluginChannelHandler handler, ServerGamePacketListenerImpl netHandler)
    {
        FriendlyByteBuf buf = PacketSplitter.receive(channel, data, netHandler);
        data.release();

        // Finished the complete packet
        if (buf != null)
        {
            handler.onPacketReceived(buf, netHandler);
            buf.release();
        }
    }

    private void schedule(Runnable task, ServerGamePacketListenerImpl netHandler)
    {
        netHandler.player.server.execute(task);
    }

    private static List<ResourceLocation> getChannels(FriendlyByteBuf buf)
    {
        buf.readerIndex(0);
        buf = PacketUtils.slice(buf);

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        String channelString = new String(bytes, Charsets.UTF_8);
        List<ResourceLocation> channels = new ArrayList<>();

        for (String channel : channelString.split("\0"))
        {
            try
            {
                ResourceLocation id = new ResourceLocation(channel);
                channels.add(id);
            }
            catch (Exception ignore) {}
        }

        return channels;
    }
}
