package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.api.revelations.WorldRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(value = WorldRenderer.class, priority = 900)
public abstract class WorldRendererMixin implements WorldRendererAccessor {
	
	@Shadow
	private BuiltChunkStorage chunks;
	
	@Shadow
	public abstract void scheduleTerrainUpdate();
	
	/**
	 * When triggered on client side lets the client redraw ALL chunks
	 * Warning: Costly + LagSpike!
	 */
	public void rebuildAllChunks() {
		if (FabricLoader.getInstance().isModLoaded("sodium")) {
			rebuildAllChunksSodium();
			return;
		}
		
		if (MinecraftClient.getInstance().world != null) {
			if (MinecraftClient.getInstance().worldRenderer != null && MinecraftClient.getInstance().player != null) {
				for (ChunkBuilder.BuiltChunk chunk : this.chunks.chunks) {
					chunk.scheduleRebuild(true);
				}
				scheduleTerrainUpdate();
			}
		}
	}
	
	private static void rebuildAllChunksSodium() {
		World world = MinecraftClient.getInstance().world;
		if (world == null) {
			return;
		}
		
		WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
		if (worldRenderer == null) {
			return;
		}
		
		WorldRendererMixinAccessor wra = (de.dafuqs.revelationary.mixin.client.WorldRendererMixinAccessor) worldRenderer;
		ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
		ChunkPos chunkPos = clientPlayerEntity.getChunkPos();
		int viewDistance = MinecraftClient.getInstance().options.getViewDistance();
		
		int startY = world.getBottomSectionCoord();
		int endY = world.getTopSectionCoord();
		
		for (int x = -viewDistance; x < viewDistance; x++) {
			for (int z = -viewDistance; z < viewDistance; z++) {
				WorldChunk chunk = MinecraftClient.getInstance().world.getChunkManager().getWorldChunk(chunkPos.x + x, chunkPos.z + z, false);
				if (chunk != null) {
					for (int y = startY; y <= endY; y++) {
						wra.invokeScheduleChunkRender(chunk.getPos().x, y, chunk.getPos().z, false);
					}
				}
			}
		}
	}
	
}