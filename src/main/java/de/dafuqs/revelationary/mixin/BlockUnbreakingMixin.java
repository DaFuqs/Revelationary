package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.api.revelations.*;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.loot.context.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(AbstractBlock.class)
public abstract class BlockUnbreakingMixin {

	/**
	 * Prevent players from accidentally mining unrevealed blocks. In no way exhaustive.
	 * Cloaked plants will still drop themselves when the block below them is broken, for example
	 */
	@Inject(method = "calcBlockBreakingDelta(Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F", at = @At("HEAD"), cancellable = true)
	public void calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if(!RevelationRegistry.isVisibleTo(state, player)) {
			cir.setReturnValue(0F);
		}
	}
	
}
