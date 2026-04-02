package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

@Mixin(BlockBehaviour.class)
public abstract class AbstractBlockMixin {
	
	@Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Ljava/util/List;", at = @At(value = "HEAD"), cancellable = true)
	private void revelationary$switchLootTableForCloakedBlock(BlockState state, LootParams.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
		BlockState cloakState = RevelationRegistry.getCloak(state);
		if (cloakState != null) {
			Player lootPlayerEntity = RevelationAware.getLootPlayerEntity(builder);
			if (!RevelationRegistry.isVisibleTo(state, lootPlayerEntity)) {
				Optional<ResourceKey<LootTable>> replacementLootTableKey = cloakState.getBlock().getLootTable();
				if(replacementLootTableKey.isPresent()) {
					LootParams lootWorldContext = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
					ServerLevel serverWorld = lootWorldContext.getLevel();
					LootTable lootTable = serverWorld.getServer().reloadableRegistries().getLootTable(replacementLootTableKey.get());
					cir.setReturnValue(lootTable.getRandomItems(lootWorldContext));
				} else {
					cir.setReturnValue(Collections.emptyList());
				}
			}
		}
	}
}
