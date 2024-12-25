package de.dafuqs.revelationary;

import de.dafuqs.revelationary.api.advancements.AdvancementHelper;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import de.dafuqs.revelationary.config.RevelationaryConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

	private static void trim() {
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
			for (Map.Entry<BlockState, BlockState> states : blockStateCloaks.entrySet()) {
				BlockState sourceBlockState = states.getKey();
				if (sourceBlockState.isAir()) {
					Revelationary.logError("Trying to register invalid block cloak. Advancement: " + advancementIdentifier
							+ " Source Block: " + Registries.BLOCK.getId(sourceBlockState.getBlock())
							+ " Target Block: " + Registries.BLOCK.getId(states.getValue().getBlock()));
					continue;
				}
				registerBlockState(advancementIdentifier, sourceBlockState, states.getValue());
			}

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
	
	// BLOCKS
	public static void registerBlockState(Identifier advancementIdentifier, BlockState sourceBlockState, BlockState targetBlockState) {
		if(advToBlockStates.containsKey(advancementIdentifier)) {
			advToBlockStates.get(advancementIdentifier).add(sourceBlockState);
		} else {
			ObjectArrayList<BlockState> list = new ObjectArrayList<>(1);
			list.add(sourceBlockState);
			advToBlockStates.put(advancementIdentifier, list);
		}

		blockStateCloaks.put(sourceBlockState, targetBlockState);
		blockStateToAdv.put(sourceBlockState, advancementIdentifier);
		blockCloaks.put(sourceBlockState.getBlock(), targetBlockState.getBlock());
		Item sourceBlockItem = sourceBlockState.getBlock().asItem();
		Item targetBlockItem = targetBlockState.getBlock().asItem();
		if (sourceBlockItem != Items.AIR && targetBlockItem != Items.AIR) {
			registerItem(advancementIdentifier, sourceBlockItem, targetBlockItem);
		}
	}
	
	public static void registerBlockTranslation(Block sourceBlock, MutableText targetTranslation) {
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
	
	public static Map<Identifier, ObjectArrayList<BlockState>> getBlockStateEntries() {
        return advToBlockStates;
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
	public static void registerItem(Identifier advancementIdentifier, Item sourceItem, Item targetItem) {
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
	
	public static void registerItemTranslation(Item sourceItem, MutableText targetTranslation) {
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
	
	public static Map<Identifier, ObjectArrayList<Item>> getItemEntries() {
		return advToItems;
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