package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import de.dafuqs.revelationary.config.RevelationaryConfig;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
	
	@Shadow
	public abstract Identifier getLootTableId();
	
	@Redirect(
			method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/loot/context/LootContext$Builder;)Ljava/util/List;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractBlock;getLootTableId()Lnet/minecraft/util/Identifier;")
	)
	private Identifier spectrum$switchLootTableForCloakedBlock(AbstractBlock instance, BlockState state, LootContext.Builder builder) {
		BlockState cloakState = RevelationRegistry.getCloak(state);
		if (cloakState != null) {
			PlayerEntity lootPlayerEntity = RevelationAware.getLootPlayerEntity(builder);
			if (!RevelationRegistry.isVisibleTo(state, lootPlayerEntity)) {
				return cloakState.getBlock().getLootTableId();
			}
		}
		return getLootTableId();
	}

	/**
	 * Prevent players from accidentally mining unrevealed blocks. In no way exhaustive.
	 * Cloaked plants will still drop themselves when the block below them is broken, for example
	 */
	@Inject(method = "calcBlockBreakingDelta(Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F", at = @At("HEAD"), cancellable = true)
	public void calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if(!RevelationRegistry.isVisibleTo(state, player) && RevelationaryConfig.CONFIG.PreventMiningOfUnrevealedBlocks) {
			cir.setReturnValue(0F);
		}
	}
	
}
