package de.dafuqs.revelationary;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.dafuqs.revelationary.api.advancements.AdvancementHelper;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import de.dafuqs.revelationary.config.RevelationaryConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.*;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Language;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RevelationRegistry {
	private static Object2ObjectOpenHashMap<Identifier, ObjectArrayList<BlockState>> advToBlockStates = new Object2ObjectOpenHashMap<>();
	private static Object2ObjectOpenHashMap<BlockState, Identifier> blockStateToAdv = new Object2ObjectOpenHashMap<>();
	private static Object2ObjectOpenHashMap<BlockState, BlockState> blockStateCloaks = new Object2ObjectOpenHashMap<>();
	private static Object2ObjectOpenHashMap<Block, Block> blockCloaks = new Object2ObjectOpenHashMap<>();

	private static Object2ObjectOpenHashMap<Identifier, ObjectArrayList<Item>> advToItems = new Object2ObjectOpenHashMap<>();
	private static Object2ObjectOpenHashMap<Item, Identifier> itemToAdv = new Object2ObjectOpenHashMap<>();
	private static Object2ObjectOpenHashMap<Item, Item> itemCloaks = new Object2ObjectOpenHashMap<>();
	
	private static Object2ObjectOpenHashMap<Block, MutableText> cloakedBlockNameTranslations = new Object2ObjectOpenHashMap<>();
	private static Object2ObjectOpenHashMap<Item, MutableText> cloakedItemNameTranslations = new Object2ObjectOpenHashMap<>();
	
	public static MutableText getTranslationString(Item item) {
		if (cloakedItemNameTranslations.containsKey(item)) {
			return cloakedItemNameTranslations.get(item);
		}
		boolean isBlockItem = item instanceof BlockItem;
		if(isBlockItem && !RevelationaryConfig.get().NameForUnrevealedBlocks.isEmpty()) {
			return Text.translatable(RevelationaryConfig.get().NameForUnrevealedBlocks);
		}
		if(!isBlockItem && !RevelationaryConfig.get().NameForUnrevealedItems.isEmpty()) {
			return Text.translatable(RevelationaryConfig.get().NameForUnrevealedItems);
		}
		if(RevelationaryConfig.get().UseTargetBlockOrItemNameInsteadOfScatter) {
			return Text.translatable(itemCloaks.get(item).getTranslationKey());
		}
		// Get the localized name of the item and scatter it using §k to make it unreadable
		return Text.literal("§k" + Language.getInstance().get(item.getTranslationKey()));
	}
	
	public static MutableText getTranslationString(Block block) {
		if (cloakedBlockNameTranslations.containsKey(block)) {
			return cloakedBlockNameTranslations.get(block);
		}
		if(!RevelationaryConfig.get().NameForUnrevealedBlocks.isEmpty()) {
			return Text.translatable(RevelationaryConfig.get().NameForUnrevealedBlocks);
		}
		if(RevelationaryConfig.get().UseTargetBlockOrItemNameInsteadOfScatter) {
			return blockCloaks.get(block).getName();
		}
		// Get the localized name of the block and scatter it using §k to make it unreadable
		return Text.literal("§k" + Language.getInstance().get(block.getTranslationKey()));
	}

	public static void trim() {
		advToBlockStates.trim();
		advToItems.trim();
		blockStateCloaks.trim();
		itemCloaks.trim();
		cloakedBlockNameTranslations.trim();
		cloakedItemNameTranslations.trim();
	}

	public static void deepTrim() {
		trim();
		for (ObjectArrayList<BlockState> blockList : advToBlockStates.values()) blockList.trim();
		for (ObjectArrayList<Item> itemList : advToItems.values()) itemList.trim();
	}
	
	private static final Set<RevelationAware> revelationAwares = new HashSet<>();
	
	public static void registerRevelationAware(RevelationAware revelationAware) {
		revelationAwares.add(revelationAware);
	}

	private static void prepareForRevelationAwaresRegistration(int amount) {
		// Items
		advToItems.ensureCapacity(advToItems.size() + amount);
		itemToAdv.ensureCapacity(itemToAdv.size() + amount);
		itemCloaks.ensureCapacity(itemCloaks.size() + amount);
		// Translations
		cloakedBlockNameTranslations.ensureCapacity(cloakedBlockNameTranslations.size() + amount);
		cloakedItemNameTranslations.ensureCapacity(cloakedItemNameTranslations.size() + amount);
	}

	public static void addRevelationAwares() {
		prepareForRevelationAwaresRegistration(revelationAwares.size());
		for (RevelationAware revelationAware : revelationAwares) {
			Identifier advancementIdentifier = revelationAware.getCloakAdvancementIdentifier();

			Map<BlockState, BlockState> blockStateCloaks = revelationAware.getBlockStateCloaks();
			ObjectArrayList<BlockState> sourceBlockStates = new ObjectArrayList<>(blockStateCloaks.size());
			ObjectArrayList<BlockState> targetBlockStates = new ObjectArrayList<>(blockStateCloaks.size());
			for (Map.Entry<BlockState, BlockState> states : blockStateCloaks.entrySet()) {
				BlockState sourceBlockState = states.getKey();
				if (!sourceBlockState.isAir()) {
					sourceBlockStates.add(sourceBlockState);
					targetBlockStates.add(states.getValue());
				} else {
					Revelationary.logError("Trying to register invalid block cloak. Advancement: " + advancementIdentifier
							+ " Source Block: " + Registries.BLOCK.getId(sourceBlockState.getBlock())
							+ " Target Block: " + Registries.BLOCK.getId(states.getValue().getBlock()));
				}
			}
			registerBlockStatesForIdentifier(advancementIdentifier, sourceBlockStates, targetBlockStates);

			Pair<Item, Item> item = revelationAware.getItemCloak();
			if (item != null) {
				registerItem(advancementIdentifier, item.getLeft(), item.getRight());
			}
			
			Pair<Block, MutableText> blockTranslation = revelationAware.getCloakedBlockTranslation();
			if (blockTranslation != null) {
				registerBlockTranslation(blockTranslation.getLeft(), blockTranslation.getRight());
			}
			Pair<Item, MutableText> itemTranslation = revelationAware.getCloakedItemTranslation();
			if (itemTranslation != null) {
				registerItemTranslation(itemTranslation.getLeft(), itemTranslation.getRight());
			}
		}
	}
	
	public static void registerFromJson(JsonObject jsonObject) {
		Identifier advancementIdentifier = Identifier.tryParse(JsonHelper.getString(jsonObject, "advancement"));
		
		if (jsonObject.has("block_states")) {
			JsonObject blockStates = jsonObject.get("block_states").getAsJsonObject();
			ObjectArrayList<BlockState> sourceBlockStates = new ObjectArrayList<>(blockStates.size());
			ObjectArrayList<BlockState> targetBlockStates = new ObjectArrayList<>(blockStates.size());
			for (Map.Entry<String, JsonElement> stateEntry : jsonObject.get("block_states").getAsJsonObject().entrySet()) {
				try {
					BlockState sourceBlockState = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), stateEntry.getKey(), true).blockState();
					BlockState targetBlockState = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), stateEntry.getValue().getAsString(), true).blockState();
					if (!sourceBlockState.isAir()) {
						sourceBlockStates.add(sourceBlockState);
						targetBlockStates.add(targetBlockState);
					} else {
						Revelationary.logError("Trying to register invalid block cloak. Advancement: " + advancementIdentifier
								+ " Source Block: " + Registries.BLOCK.getId(sourceBlockState.getBlock())
								+ " Target Block: " + Registries.BLOCK.getId(targetBlockState.getBlock()));
					}
				} catch (Exception e) {
					Revelationary.logError("Error parsing block state: " + e);
				}
			}
			registerBlockStatesForIdentifier(advancementIdentifier, sourceBlockStates, targetBlockStates);
		}
		if (jsonObject.has("items")) {
			for (Map.Entry<String, JsonElement> itemEntry : jsonObject.get("items").getAsJsonObject().entrySet()) {
				Identifier sourceId = Identifier.tryParse(itemEntry.getKey());
				Identifier targetId = Identifier.tryParse(itemEntry.getValue().getAsString());
				
				Item sourceItem = Registries.ITEM.get(sourceId);
				Item targetItem = Registries.ITEM.get(targetId);
				
				registerItem(advancementIdentifier, sourceItem, targetItem);
			}
		}
		if (jsonObject.has("block_name_replacements")) {
			for (Map.Entry<String, JsonElement> blockNameEntry : jsonObject.get("block_name_replacements").getAsJsonObject().entrySet()) {
				Identifier sourceId = Identifier.tryParse(blockNameEntry.getKey());
				MutableText targetText = Text.translatable(blockNameEntry.getValue().getAsString());
				
				Block sourceBlock = Registries.BLOCK.get(sourceId);
				cloakedBlockNameTranslations.put(sourceBlock, targetText);
				
				Item blockItem = sourceBlock.asItem();
				if (blockItem != null && blockItem != Items.AIR) {
					cloakedItemNameTranslations.put(blockItem, targetText);
				}
			}
		}
		if (jsonObject.has("item_name_replacements")) {
			for (Map.Entry<String, JsonElement> itemNameEntry : jsonObject.get("item_name_replacements").getAsJsonObject().entrySet()) {
				Identifier sourceId = Identifier.tryParse(itemNameEntry.getKey());
				MutableText targetText = Text.translatable(itemNameEntry.getValue().getAsString());
				
				Item sourceItem = Registries.ITEM.get(sourceId);
				cloakedItemNameTranslations.put(sourceItem, targetText);
			}
		}
	}
	
	// BLOCKS
	private static void registerBlockStatesForIdentifier(Identifier advancementIdentifier, ObjectArrayList<BlockState> sourceBlockStates, ObjectArrayList<BlockState> targetBlockStates) {
		if (sourceBlockStates.size() != targetBlockStates.size()) throw new IllegalArgumentException("Unequal sizes of sourceBlockStates and targetBlockStates arrays");
		int sz = sourceBlockStates.size();
		if(advToBlockStates.containsKey(advancementIdentifier)) {
			ObjectArrayList<BlockState> blockStates = advToBlockStates.get(advancementIdentifier);
			blockStates.ensureCapacity(blockStates.size() + sz); // preallocate
			blockStates.addAll(sourceBlockStates);
		} else advToBlockStates.put(advancementIdentifier, sourceBlockStates);

		blockStateCloaks.ensureCapacity(blockStateCloaks.size() + sz);
		blockStateToAdv.ensureCapacity(blockStateToAdv.size() + sz);
		// assume amount of blocks is roughly equal to amount of blockstates (in real case scenario)
		blockCloaks.ensureCapacity(blockCloaks.size() + sz);
		// assume amount of items is roughly equal to amount of blockstates (in real case scenario)
		ObjectArrayList<Item> sourceItems = new ObjectArrayList<>(sz);
		ObjectArrayList<Item> targetItems = new ObjectArrayList<>(sz);
		for (int i = 0; i < sz; i++) {
			BlockState sourceBlockState = sourceBlockStates.get(i);
			BlockState targetBlockState = targetBlockStates.get(i);
			blockStateCloaks.put(sourceBlockState, targetBlockState);
			blockStateToAdv.put(sourceBlockState, advancementIdentifier);
			blockCloaks.put(sourceBlockState.getBlock(), targetBlockState.getBlock());
			Item sourceBlockItem = sourceBlockState.getBlock().asItem();
			Item targetBlockItem = targetBlockState.getBlock().asItem();
			if (sourceBlockItem != Items.AIR && targetBlockItem != Items.AIR) {
				sourceItems.add(sourceBlockItem);
				targetItems.add(targetBlockItem);
			}
		}
		registerItemsForIdentifier(advancementIdentifier, sourceItems, targetItems);
	}
	
	private static void registerBlockTranslation(Block sourceBlock, MutableText targetTranslation) {
		cloakedBlockNameTranslations.put(sourceBlock, targetTranslation);
	}
	
	public static boolean hasCloak(BlockState blockState) {
		return blockStateCloaks.containsKey(blockState);
	}
	
	public static boolean isVisibleTo(BlockState state, PlayerEntity player) {
		return AdvancementHelper.hasAdvancement(player, blockStateToAdv.getOrDefault(state, null));
	}
	
	public static @NotNull Collection<BlockState> getRevealedBlockStates(Identifier advancement) {
		List<BlockState> blockStates = new ArrayList<>();
		if (advToBlockStates.containsKey(advancement)) {
			for (Object entry : advToBlockStates.get(advancement)) {
				if (entry instanceof BlockState blockState) {
					blockStates.add(blockState);
				}
			}
		}
		return blockStates;
	}
	
	public static Map<Identifier, List<BlockState>> getBlockStateEntries() {
		// fighting invariance of java generic types
        return (Map<Identifier, List<BlockState>>) (Map<?, ?>) advToBlockStates;
	}
	
	public static List<BlockState> getBlockStateEntries(Identifier advancement) {
		return advToBlockStates.getOrDefault(advancement, ObjectArrayList.of());
	}
	
	public static List<Block> getBlockEntries() {
		List<Block> blocks = new ArrayList<>();
		for (List<BlockState> states : advToBlockStates.values()) {
			for (BlockState state : states) {
				Block block = state.getBlock();
				if (!blocks.contains(block)) {
					blocks.add(block);
				}
			}
		}
		return blocks;
	}
	
	public static List<Block> getBlockEntries(Identifier advancement) {
		if (advToBlockStates.containsKey(advancement)) {
			List<BlockState> states = advToBlockStates.get(advancement);
			List<Block> blocks = new ArrayList<>();
			for (BlockState state : states) {
				Block block = state.getBlock();
				if (!blocks.contains(block)) {
					blocks.add(block);
				}
			}
			return blocks;
		} else {
			return new ArrayList<>();
		}
	}
	
	// ITEMS
	private static void registerItem(Identifier advancementIdentifier, Item sourceItem, Item targetItem) {
		if(sourceItem == Items.AIR || targetItem == Items.AIR) {
			Revelationary.logError("Trying to register invalid item cloak. Advancement: " + advancementIdentifier
					+ " Source Item: " + Registries.ITEM.getId(sourceItem)
					+ " Target Item: " + Registries.ITEM.getId(targetItem));
			return;
		}
		
		if (advToItems.containsKey(advancementIdentifier)) {
			ObjectArrayList<Item> list = advToItems.get(advancementIdentifier);
			if (list.contains(sourceItem)) {
				return;
			}
			list.add(sourceItem);
		} else {
			ObjectArrayList<Item> list = new ObjectArrayList<>();
			list.add(sourceItem);
			advToItems.put(advancementIdentifier, list);
		}
		itemCloaks.put(sourceItem, targetItem);
		itemToAdv.put(sourceItem, advancementIdentifier);
	}

	private static void registerItemsForIdentifier(Identifier advancementIdentifier, ObjectArrayList<Item> sourceItems, ObjectArrayList<Item> targetItems) {
		if (sourceItems.size() != targetItems.size()) throw new IllegalArgumentException("Unequal sizes of sourceItems and targetItems arrays");
		int sz = sourceItems.size();
		if (advToItems.containsKey(advancementIdentifier)) {
			ObjectArrayList<Item> items = advToItems.get(advancementIdentifier);
			items.ensureCapacity(items.size() + sz);
			items.addAll(sourceItems);
		} else advToItems.put(advancementIdentifier, sourceItems);

		itemCloaks.ensureCapacity(itemCloaks.size() + sz);
		itemToAdv.ensureCapacity(itemToAdv.size() + sz);
		for (int i = 0; i < sz; i++) {
			Item sourceItem = sourceItems.get(i);
			itemCloaks.put(sourceItem, targetItems.get(i));
			itemToAdv.put(sourceItem, advancementIdentifier);
		}
	}
	
	private static void registerItemTranslation(Item sourceItem, MutableText targetTranslation) {
		cloakedItemNameTranslations.put(sourceItem, targetTranslation);
	}
	
	public static boolean hasCloak(Item item) {
		return itemCloaks.containsKey(item);
	}
	
	@Nullable
	public static Item getCloak(Item item) {
		return itemCloaks.getOrDefault(item, null);
	}
	
	public static boolean isVisibleTo(Item item, PlayerEntity player) {
		return AdvancementHelper.hasAdvancement(player, itemToAdv.getOrDefault(item, null));
	}
	
	@Nullable
	public static BlockState getCloak(BlockState blockState) {
		return blockStateCloaks.getOrDefault(blockState, null);
	}
	
	public static @NotNull Collection<Item> getRevealedItems(Identifier advancement) {
		if (advToItems.containsKey(advancement)) return advToItems.get(advancement).clone();
		return ObjectArrayList.of();
	}
	
	public static Map<Identifier, List<Item>> getItemEntries() {
		// fighting invariance of java generic types
		return (Map<Identifier, List<Item>>) (Map<?,?>) advToItems;
	}
	
	public static List<Item> getItemEntries(Identifier advancement) {
		return advToItems.getOrDefault(advancement, ObjectArrayList.of());
	}

	public static void fromPacket(RevelationaryNetworking.RevelationSync syncPacket) {
		advToBlockStates = syncPacket.advToBlockStates();
		blockStateToAdv = syncPacket.blockStateToAdv();
		blockStateCloaks = syncPacket.blockStateCloaks();
		blockCloaks = syncPacket.blockCloaks();
		advToItems = syncPacket.advToItems();
		itemToAdv = syncPacket.itemToAdv();
		itemCloaks = syncPacket.itemCloaks();
		cloakedBlockNameTranslations = syncPacket.cloakedBlockNameTranslations();
		cloakedItemNameTranslations = syncPacket.cloakedItemNameTranslations();

		RevelationRegistry.addRevelationAwares();
		RevelationRegistry.deepTrim();
	}

	public static RevelationaryNetworking.RevelationSync intoPacket() {
		return new RevelationaryNetworking.RevelationSync(advToBlockStates,
													   blockStateToAdv,
													   blockStateCloaks,
													   blockCloaks,
													   advToItems,
													   itemToAdv,
													   itemCloaks,
													   cloakedBlockNameTranslations,
													   cloakedItemNameTranslations);
	}
}