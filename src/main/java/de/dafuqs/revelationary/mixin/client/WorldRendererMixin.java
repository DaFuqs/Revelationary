package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.api.revelations.WorldRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
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
		if (MinecraftClient.getInstance().world != null) {
			if (MinecraftClient.getInstance().worldRenderer != null && MinecraftClient.getInstance().player != null) {
				for (ChunkBuilder.BuiltChunk chunk : this.chunks.chunks) {
					chunk.scheduleRebuild(true);
				}
				scheduleTerrainUpdate();
			}
		}
	}
	
}