package de.dafuqs.revelationary.mixin.client;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface WorldRendererMixinAccessor {
	
	@Invoker("scheduleChunkRender")
	void invokeScheduleChunkRender(int x, int y, int z, boolean important);
	
}