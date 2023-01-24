package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.api.revelations.*;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.loot.context.*;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

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


}
