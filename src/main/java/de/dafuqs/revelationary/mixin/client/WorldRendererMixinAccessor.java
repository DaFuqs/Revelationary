package de.dafuqs.revelationary.mixin.client;

import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(LevelRenderer.class)
public interface WorldRendererMixinAccessor {
	@Invoker("Lnet/minecraft/client/renderer/LevelRenderer;setSectionDirty(IIIZ)V")
	void invokeSetSectionDirty(int x, int y, int z, boolean important);
}