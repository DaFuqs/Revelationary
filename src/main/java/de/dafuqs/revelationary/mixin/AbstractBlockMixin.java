package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
	
	@Shadow
	public abstract Identifier getLootTableId();

	@Redirect(
			method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/loot/context/LootContextParameterSet$Builder;)Ljava/util/List;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractBlock;getLootTableId()Lnet/minecraft/util/Identifier;")
	)
	private Identifier spectrum$switchLootTableForCloakedBlock(AbstractBlock instance, BlockState state, LootContextParameterSet.Builder builder) {
		BlockState cloakState = RevelationRegistry.getCloak(state);
		if (cloakState != null) {
			PlayerEntity lootPlayerEntity = RevelationAware.getLootPlayerEntity(builder);
			if (!RevelationRegistry.isVisibleTo(state, lootPlayerEntity)) {
				return cloakState.getBlock().getLootTableId();
			}
		}
		return getLootTableId();
	}

}
