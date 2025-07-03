package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(BlockBehaviour.class)
public abstract class BlockUnbreakingMixin {
	/**
	 * Prevent players from accidentally mining unrevealed blocks. In no way exhaustive.
	 * Cloaked plants will still drop themselves when the block below them is broken, for example
	 */
	@Inject(method = "getDestroyProgress(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F", at = @At("HEAD"), cancellable = true)
	public void revelationary$calcBlockBreakingDelta(BlockState state, Player player, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if(!RevelationRegistry.isVisibleTo(state, player)) {
			cir.setReturnValue(0F);
		}
	}
}
