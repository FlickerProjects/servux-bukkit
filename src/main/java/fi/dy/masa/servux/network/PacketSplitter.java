package fi.dy.masa.servux.network;

import fi.dy.masa.servux.network.util.PacketUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Network packet splitter code from QuickCarpet by skyrising
 * @author skyrising
 */
public class PacketSplitter
{
    public static final int MAX_TOTAL_PER_PACKET_S2C = 1048576;
    public static final int MAX_PAYLOAD_PER_PACKET_S2C = MAX_TOTAL_PER_PACKET_S2C - 5;
    public static final int DEFAULT_MAX_RECEIVE_SIZE_C2S = 1048576;

    private static final Map<Pair<PacketListener, ResourceLocation>, ReadingSession> READING_SESSIONS = new HashMap<>();

    public static void send(FriendlyByteBuf packet, ResourceLocation channel, ServerGamePacketListenerImpl networkHandler)
    {
        send(packet, MAX_PAYLOAD_PER_PACKET_S2C, buf -> networkHandler.send(new ClientboundCustomPayloadPacket(channel, buf)));
    }

    private static void send(FriendlyByteBuf packet, int payloadLimit, Consumer<FriendlyByteBuf> sender)
    {
        int len = packet.writerIndex();

        packet.readerIndex(0);

        for (int offset = 0; offset < len; offset += payloadLimit)
        {
            int thisLen = Math.min(len - offset, payloadLimit);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(thisLen));

            if (offset == 0)
            {
                buf.writeVarInt(len);
            }

            buf.writeBytes(packet, thisLen);
            sender.accept(buf);
        }

        packet.release();
    }

    @Nullable
    public static FriendlyByteBuf receive(ResourceLocation channel, FriendlyByteBuf data, ServerGamePacketListenerImpl networkHandler)
    {
        return receive(channel, data, DEFAULT_MAX_RECEIVE_SIZE_C2S, networkHandler);
    }

    @Nullable
    private static FriendlyByteBuf receive(ResourceLocation channel, FriendlyByteBuf data, int maxLength, ServerGamePacketListenerImpl networkHandler)
    {
        Pair<PacketListener, ResourceLocation> key = Pair.of(networkHandler, channel);
        return READING_SESSIONS.computeIfAbsent(key, ReadingSession::new).receive(data, maxLength);
    }

    /**
     * Sends a packet type ID as a VarInt, and then the given Compound tag.
     */
    public static void sendPacketTypeAndCompound(ResourceLocation channel, int packetType, CompoundTag data, ServerPlayer player)
    {
        sendPacketTypeAndCompound(channel, packetType, data, player.connection);
    }

    /**
     * Sends a packet type ID as a VarInt, and then the given Compound tag.
     */
    public static void sendPacketTypeAndCompound(ResourceLocation channel, int packetType, CompoundTag data, ServerGamePacketListenerImpl networkHandler)
    {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(packetType);
        buf.writeNbt(data);

        send(buf, channel, networkHandler);
    }

    private static class ReadingSession
    {
        private final Pair<PacketListener, ResourceLocation> key;
        private int expectedSize = -1;
        private FriendlyByteBuf received;

        private ReadingSession(Pair<PacketListener, ResourceLocation> key)
        {
            this.key = key;
        }

        @Nullable
        private FriendlyByteBuf receive(FriendlyByteBuf data, int maxLength)
        {
            data.readerIndex(0);
            data = PacketUtils.slice(data);

            if (this.expectedSize < 0)
            {
                this.expectedSize = data.readVarInt();

                if (this.expectedSize > maxLength)
                {
                    throw new IllegalArgumentException("Payload too large");
                }

                this.received = new FriendlyByteBuf(Unpooled.buffer(this.expectedSize));
            }

            this.received.writeBytes(data.readBytes(data.readableBytes()));

            if (this.received.writerIndex() >= this.expectedSize)
            {
                READING_SESSIONS.remove(this.key);
                return this.received;
            }

            return null;
        }
    }
}
