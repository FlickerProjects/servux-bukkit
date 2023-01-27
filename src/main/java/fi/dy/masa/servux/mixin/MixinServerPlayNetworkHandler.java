package fi.dy.masa.servux.mixin;

import fi.dy.masa.servux.network.ServerPacketChannelHandler;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 998)
public abstract class MixinServerPlayNetworkHandler
{
    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void servux_handleCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci)
    {
        ServerPacketChannelHandler.INSTANCE.processPacketFromClient(packet, (ServerGamePacketListenerImpl) (Object) this);
    }
}
