package fi.dy.masa.servux.mixin;

import fi.dy.masa.servux.dataproviders.StructureDataProvider;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ChunkMap.class)
public abstract class MixinThreadedAnvilChunkStorage
{
    @Inject(method = "playerLoadedChunk", at = @At("HEAD"))
    private void servux_onSendChunkPacket(ServerPlayer player,
                                          MutableObject<Map<Object, ClientboundLevelChunkWithLightPacket>> cachedDataPackets,
                                          LevelChunk chunk,
                                          CallbackInfo ci)
    {
        if (StructureDataProvider.INSTANCE.isEnabled())
        {
            StructureDataProvider.INSTANCE.onStartedWatchingChunk(player, chunk);
        }
    }
}
