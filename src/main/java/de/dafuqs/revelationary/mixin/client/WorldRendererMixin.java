package de.dafuqs.revelationary.mixin.client;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.api.revelations.*;
import net.minecraft.client.*;
import net.minecraft.client.player.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.*;
import org.spongepowered.asm.mixin.*;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class WorldRendererMixin implements WorldRendererAccessor {
	@Shadow
	private ViewArea viewArea;
	
	@Shadow
	public abstract void allChanged();
	
	/**
	 * When triggered on client side lets the client redraw ALL chunks
	 * Warning: Costly + LagSpike!
	 */
	public void revelationary$rebuildAllChunks() {
		if (Revelationary.cursedChunkBuildingActive()) {
			revelationary$rebuildAllChunksSodium();
			return;
		}
		
		if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null) {
			for (SectionRenderDispatcher.RenderSection chunk : this.viewArea.sections) {
				chunk.setDirty(true);
			}
			
			allChanged();
		}
	}
	
	@Unique
	private static void revelationary$rebuildAllChunksSodium() {
		Level world = Minecraft.getInstance().level;
		if (world == null) {
			return;
		}
		
		LevelRenderer worldRenderer = Minecraft.getInstance().levelRenderer;
		
		WorldRendererMixinAccessor wra = (WorldRendererMixinAccessor) worldRenderer;
		LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;
		ChunkPos chunkPos = clientPlayerEntity.chunkPosition();
		int viewDistance = Minecraft.getInstance().options.renderDistance().get();
		
		int startY = world.getMinSection();
		int endY = world.getMaxSection();
		
		for (int x = -viewDistance; x < viewDistance; x++) {
			for (int z = -viewDistance; z < viewDistance; z++) {
				LevelChunk chunk = Minecraft.getInstance().level.getChunkSource().getChunk(chunkPos.x + x, chunkPos.z + z, false);
				if (chunk != null) {
					for (int y = startY; y <= endY; y++) {
						wra.invokeSetSectionDirty(chunk.getPos().x, y, chunk.getPos().z, false);
					}
				}
			}
		}
	}
}