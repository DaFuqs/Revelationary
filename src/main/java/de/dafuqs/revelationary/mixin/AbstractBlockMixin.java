package de.dafuqs.revelationary.mixin;

import de.dafuqs.revelationary.*;
import de.dafuqs.revelationary.api.revelations.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.storage.loot.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(BlockBehaviour.class)
public abstract class AbstractBlockMixin {
	@Shadow
	public abstract ResourceKey<LootTable> getLootTable();

	@Redirect(
			method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Ljava/util/List;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockBehaviour;getLootTable()Lnet/minecraft/resources/ResourceKey;")
	)
	private ResourceKey<LootTable> revelationary$switchLootTableForCloakedBlock(BlockBehaviour instance, BlockState state, LootParams.Builder builder) {
		BlockState cloakState = RevelationRegistry.getCloak(state);
		if (cloakState != null) {
			Player lootPlayerEntity = RevelationAware.getLootPlayerEntity(builder);
			if (!RevelationRegistry.isVisibleTo(state, lootPlayerEntity)) {
				return cloakState.getBlock().getLootTable();
			}
		}
		return getLootTable();
	}
}
