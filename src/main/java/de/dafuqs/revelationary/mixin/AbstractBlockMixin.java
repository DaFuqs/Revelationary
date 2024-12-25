package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.loot.context.*;
import net.minecraft.registry.*;
import net.minecraft.server.world.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
	
	@Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/loot/context/LootWorldContext$Builder;)Ljava/util/List;", at = @At(value = "HEAD"), cancellable = true)
	private void revelationary$switchLootTableForCloakedBlock(BlockState state, LootWorldContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
		BlockState cloakState = RevelationRegistry.getCloak(state);
		if (cloakState != null) {
			PlayerEntity lootPlayerEntity = RevelationAware.getLootPlayerEntity(builder);
			if (!RevelationRegistry.isVisibleTo(state, lootPlayerEntity)) {
				Optional<RegistryKey<LootTable>> replacementLootTableKey = cloakState.getBlock().getLootTableKey();
				if(replacementLootTableKey.isPresent()) {
					LootWorldContext lootWorldContext = builder.add(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
					ServerWorld serverWorld = lootWorldContext.getWorld();
					LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(replacementLootTableKey.get());
					cir.setReturnValue(lootTable.generateLoot(lootWorldContext));
				} else {
					cir.setReturnValue(Collections.emptyList());
				}
			}
		}
	}
}
