package de.dafuqs.revelationary.api.revelations;

import de.dafuqs.revelationary.RevelationRegistry;
import de.dafuqs.revelationary.api.advancements.AdvancementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Interface for defining a block/item/blockitem/... as revealable.
 * Using this interface will allow more functionality than using Revelationary's json api
 * <p>
 * Blocks and items with this interface will disguise themselves as other blocks/items
 * until the player gets a specific advancement. It's name will get obfuscated.
 * Disguised blocks will drop item stacks as if they were the block they are disguised as
 */
public interface RevelationAware {
	
	/**
	 * The advancement identifier that the player must have to see this block/item
	 */
	Identifier getCloakAdvancementIdentifier();
	
	/**
	 * Register this object as revealable
	 * You can call this safely at the end of the objects' constructor.
	 */
	static void register(RevelationAware revelationAware) {
		RevelationRegistry.registerRevelationAware(revelationAware);
	}
	
	/**
	 * A map of all blockstates and the block states disguised as
	 * This can be a single entry like this.getDefaultState() => Blocks.STONE.getDefaultState()
	 * Or if your block has > 1 state a mapping for every state that should be disguised
	 * If you use this interface on an item without block representation (no BlockItem) return an emtpy map
	 */
	Map<BlockState, BlockState> getBlockStateCloaks();
	
	/**
	 * A pair consisting of this objects item representation and the item it should be disguised as
	 * If you are implementing an item without a block this will prob. look something like "new Pair<>(this, cloakItem);"
	 * If you are implementing a BlockItem something like "new Pair<>(this.asItem(), Blocks.OAK_LOG.asItem())"
	 * If you use this interace on a block without item representation (like vanilla end portal) return null
	 */
	@Nullable Pair<Item, Item> getItemCloak();
	
	/**
	 * Optionally return a mapping of a revelation aware item and the text that should be used as translation
	 * If you return null (the default) it's name will be scattered unreadable instead
	 *
	 * @return the matching of the item and the text it will use when not revealed
	 */
	@Nullable
	default Pair<Item, MutableText> getCloakedItemTranslation() {
		return null;
	}
	
	/**
	 * Optionally return a mapping of a revelation aware block and the text that should be used as translation
	 * If you return null (the default) it's name will be scattered unreadable instead
	 *
	 * @return the matching of the block and the text it will use when not revealed
	 */
	@Nullable
	default Pair<Block, MutableText> getCloakedBlockTranslation() {
		return null;
	}
	
	/**
	 * Gets called when this object gets disguised (like when taking an advancement from the player)
	 */
	default void onCloak() {
	}
	
	/**
	 * Gets called when this object gets revealed (when the player gets the matching advancement)
	 */
	default void onUncloak() {
	}
	
	/**
	 * Helper method that checks, if the ShapeContext is of a player and if the player has the matching advancement
	 *
	 * @param context the ShapeContext to check
	 */
	default boolean isVisibleTo(ShapeContext context) {
		if (context instanceof EntityShapeContext) {
			Entity entity = ((EntityShapeContext) context).getEntity();
			if (entity instanceof PlayerEntity) {
				return this.isVisibleTo((PlayerEntity) entity);
			}
		}
		return true;
	}
	
	/**
	 * Helper method that checks, if the player has the matching advancement
	 *
	 * @param player the player to check
	 */
	default boolean isVisibleTo(@Nullable PlayerEntity player) {
		return AdvancementHelper.hasAdvancement(player, getCloakAdvancementIdentifier());
	}
	
	
	/**
	 * Helper method that returns the player in a lootContextBuilder
	 *
	 * @param lootContextBuilder The loot context builder to search a player in
	 * @return the player of that loot context builder. null if there is no player in that context
	 */
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