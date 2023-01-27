package fi.dy.masa.servux.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public interface IPluginChannelHandler
{
    ResourceLocation getChannel();

    default void onPacketReceived(FriendlyByteBuf buf, ServerGamePacketListenerImpl netHandler)
    {
    }

    default boolean isSubscribable()
    {
        return false;
    }

    default boolean subscribe(ServerGamePacketListenerImpl netHandler)
    {
        return false;
    }

    default boolean unsubscribe(ServerGamePacketListenerImpl netHandler)
    {
        return false;
    }
}
