package fi.dy.masa.servux.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundCustomPayloadPacket.class)
public interface IMixinCustomPayloadC2SPacket
{
    @Accessor("identifier")
    ResourceLocation servux_getChannel();

    @Accessor("data")
    FriendlyByteBuf servux_getData();
}
