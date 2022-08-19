package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.api.revelations.WorldRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(value = WorldRenderer.class, priority = 900)
public abstract class WorldRendererMixin implements WorldRendererAccessor {
	
	@Shadow
	private BuiltChunkStorage chunks;
	
	/**
	 * When triggered on client side lets the client redraw ALL chunks
	 * Warning: Costly + LagSpike!
	 */
	public void rebuildAllChunks() {
		World world = MinecraftClient.getInstance().world;
		if (world != null) {
			WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
			if (worldRenderer != null) {
				for (ChunkBuilder.BuiltChunk chunk : this.chunks.chunks) {
					int startY = world.getBottomSectionCoord();
					int endY = world.getTopSectionCoord();
					for (int y = startY; y <= endY; y++) {
						((de.dafuqs.revelationary.mixin.client.WorldRendererAccessor) worldRenderer).invokeScheduleChunkRender(chunk.getOrigin().getX(), startY + y, chunk.getOrigin().getZ(), false);
					}
				}
			}
		}
	}
	
}