package de.dafuqs.revelationary.api.revelations;

import de.dafuqs.revelationary.RevelationRegistry;
import de.dafuqs.revelationary.api.advancements.AdvancementHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface RevelationAware {
	
	Identifier getCloakAdvancementIdentifier();
	
	static void register(RevelationAware revelationAware) {
		RevelationRegistry.registerRevelationAware(revelationAware);
	}
	
	Map<BlockState, BlockState> getBlockStateCloaks();
	
	@Nullable Pair<Item, Item> getItemCloak();
	
	default void onCloak() {
	}
	
	default void onUncloak() {
	}
	
	default boolean isVisibleTo(ShapeContext context) {
		if (context instanceof EntityShapeContext) {
			Entity entity = ((EntityShapeContext) context).getEntity();
			if (entity instanceof PlayerEntity) {
				return this.isVisibleTo((PlayerEntity) entity);
			}
		}
		return true;
	}
	
	default boolean isVisibleTo(PlayerEntity playerEntity) {
		return AdvancementHelper.hasAdvancement(playerEntity, getCloakAdvancementIdentifier());
	}
	
	@Nullable
	static PlayerEntity getLootPlayerEntity(LootContext.Builder lootContextBuilder) {
		if (lootContextBuilder.getNullable(LootContextParameters.THIS_ENTITY) == null) {
			return null;
		} else {
			Entity entity = lootContextBuilder.get(LootContextParameters.THIS_ENTITY);
			if (entity instanceof PlayerEntity) {
				return (PlayerEntity) entity;
			} else {
				return null;
			}
		}
	}
	
}
