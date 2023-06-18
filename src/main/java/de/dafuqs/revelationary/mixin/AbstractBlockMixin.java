package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import de.dafuqs.revelationary.config.RevelationaryConfig;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
	
	@Shadow
	public abstract Identifier getLootTableId();

	@Shadow protected abstract Block asBlock();

	private LootContextParameterSet.Builder spectrum$builder;

	@Redirect(
			method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/loot/context/LootContextParameterSet$Builder;)Ljava/util/List;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractBlock;getLootTableId()Lnet/minecraft/util/Identifier;")
	)
	private Identifier spectrum$switchLootTableForCloakedBlock(AbstractBlock instance) {
		BlockState state = this.asBlock().getDefaultState();
		BlockState cloakState = RevelationRegistry.getCloak(state);
		if (cloakState != null) {
			PlayerEntity lootPlayerEntity = RevelationAware.getLootPlayerEntity(spectrum$builder);
			if (!RevelationRegistry.isVisibleTo(state, lootPlayerEntity)) {
				return cloakState.getBlock().getLootTableId();
			}
		}
		return getLootTableId();
	}

	// unsure if this works, but it should
	@Inject(method = "getDroppedStacks", at = @At("HEAD"))
	public final void spectrum$getBuilder(BlockState state, LootContextParameterSet.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
		spectrum$builder = builder;
	}
}
