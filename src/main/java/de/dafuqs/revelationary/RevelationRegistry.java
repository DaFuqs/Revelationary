package de.dafuqs.revelationary;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.dafuqs.revelationary.api.advancements.AdvancementHelper;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Language;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RevelationRegistry {
	
	private static final Map<Identifier, List<BlockState>> ADVANCEMENT_BLOCK_REGISTRY = new HashMap<>();
	private static final Map<BlockState, Identifier> BLOCK_ADVANCEMENT_REGISTRY = new HashMap<>();
	private static final Map<BlockState, BlockState> BLOCK_STATE_REGISTRY = new HashMap<>();
	
	private static final Map<Identifier, List<Item>> ADVANCEMENT_ITEM_REGISTRY = new HashMap<>();
	private static final Map<Item, Identifier> ITEM_ADVANCEMENT_REGISTRY = new HashMap<>();
	private static final Map<Item, Item> ITEM_REGISTRY = new HashMap<>();
	
	private static final Map<Block, MutableText> ALTERNATE_BLOCK_TRANSLATION_STRING_REGISTRY = new HashMap<>();
	private static final Map<Item, MutableText> ALTERNATE_ITEM_TRANSLATION_STRING_REGISTRY = new HashMap<>();
	
	public static MutableText getTranslationString(Item item) {
		if (ALTERNATE_ITEM_TRANSLATION_STRING_REGISTRY.containsKey(item)) {
			return ALTERNATE_ITEM_TRANSLATION_STRING_REGISTRY.get(item);
		} else {
			// Get the localized name of the block and scatter it using §k to make it unreadable
			Language language = Language.getInstance();
			return new LiteralText("§k" + language.get(item.getTranslationKey()));
		}
	}
	
	public static MutableText getTranslationString(Block block) {
		if (ALTERNATE_BLOCK_TRANSLATION_STRING_REGISTRY.containsKey(block)) {
			return ALTERNATE_BLOCK_TRANSLATION_STRING_REGISTRY.get(block);
		} else {
			// Get the localized name of the block and scatter it using §k to make it unreadable
			Language language = Language.getInstance();
			return new LiteralText("§k" + language.get(block.getTranslationKey()));
		}
	}
	
	public static void clear() {
		ADVANCEMENT_BLOCK_REGISTRY.clear();
		ADVANCEMENT_ITEM_REGISTRY.clear();
		BLOCK_STATE_REGISTRY.clear();
		ITEM_REGISTRY.clear();
		ALTERNATE_BLOCK_TRANSLATION_STRING_REGISTRY.clear();
		ALTERNATE_ITEM_TRANSLATION_STRING_REGISTRY.clear();
	}
	
	private static final Set<RevelationAware> revelationAwares = new HashSet<>();
	
	public static void registerRevelationAware(RevelationAware revelationAware) {
		revelationAwares.add(revelationAware);
	}
	
	public static void addRevelationAwares() {
		for (RevelationAware revelationAware : revelationAwares) {
			Identifier advancementIdentifier = revelationAware.getCloakAdvancementIdentifier();
			for (Map.Entry<BlockState, BlockState> states : revelationAware.getBlockStateCloaks().entrySet()) {
				registerBlockState(advancementIdentifier, states.getKey(), states.getValue());
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
	
	public static void registerFromJson(JsonObject jsonObject) {
		Identifier advancementIdentifier = Identifier.tryParse(JsonHelper.getString(jsonObject, "advancement"));
		
		if (jsonObject.has("block_states")) {
			for (Map.Entry<String, JsonElement> stateEntry : jsonObject.get("block_states").getAsJsonObject().entrySet()) {
				try {
					BlockState sourceBlockState = new BlockArgumentParser(new StringReader(stateEntry.getKey()), false).parse(false).getBlockState();
					BlockState targetBlockState = new BlockArgumentParser(new StringReader(stateEntry.getValue().getAsString()), false).parse(false).getBlockState();
					
					registerBlockState(advancementIdentifier, sourceBlockState, targetBlockState);
				} catch (Exception e) {
					Revelationary.logError("Error parsing block state: " + e);
				}
			}
		}
		if (jsonObject.has("items")) {
			for (Map.Entry<String, JsonElement> itemEntry : jsonObject.get("items").getAsJsonObject().entrySet()) {
				Identifier sourceId = Identifier.tryParse(itemEntry.getKey());
				Identifier targetId = Identifier.tryParse(itemEntry.getValue().getAsString());
				
				Item sourceItem = Registry.ITEM.get(sourceId);
				Item targetItem = Registry.ITEM.get(targetId);
				
				registerItem(advancementIdentifier, sourceItem, targetItem);
			}
		}
		if (jsonObject.has("block_name_replacements")) {
			for (Map.Entry<String, JsonElement> blockNameEntry : jsonObject.get("block_name_replacements").getAsJsonObject().entrySet()) {
				Identifier sourceId = Identifier.tryParse(blockNameEntry.getKey());
				TranslatableText targetText = new TranslatableText(blockNameEntry.getValue().getAsString());
				
				Block sourceBlock = Registry.BLOCK.get(sourceId);
				ALTERNATE_BLOCK_TRANSLATION_STRING_REGISTRY.put(sourceBlock, targetText);
				
				Item blockItem = sourceBlock.asItem();
				if (blockItem != null && blockItem != Items.AIR) {
					ALTERNATE_ITEM_TRANSLATION_STRING_REGISTRY.put(blockItem, targetText);
				}
			}
		}
		if (jsonObject.has("item_name_replacements")) {
			for (Map.Entry<String, JsonElement> itemNameEntry : jsonObject.get("item_name_replacements").getAsJsonObject().entrySet()) {
				Identifier sourceId = Identifier.tryParse(itemNameEntry.getKey());
				MutableText targetText = new TranslatableText(itemNameEntry.getValue().getAsString());
				
				Item sourceItem = Registry.ITEM.get(sourceId);
				ALTERNATE_ITEM_TRANSLATION_STRING_REGISTRY.put(sourceItem, targetText);
			}
		}
	}
	
	// BLOCKS
	private static void registerBlockState(Identifier advancementIdentifier, BlockState sourceBlockState, BlockState targetBlockState) {
		List<BlockState> list;
		if (ADVANCEMENT_BLOCK_REGISTRY.containsKey(advancementIdentifier)) {
			list = ADVANCEMENT_BLOCK_REGISTRY.get(advancementIdentifier);
			list.add(sourceBlockState);
		} else {
			list = new ArrayList<>();
			list.add(sourceBlockState);
			ADVANCEMENT_BLOCK_REGISTRY.put(advancementIdentifier, list);
		}
		
		Item sourceBlockItem = sourceBlockState.getBlock().asItem();
		if (sourceBlockItem != Items.AIR) {
			Item targetBlockItem = targetBlockState.getBlock().asItem();
			if (targetBlockItem != Items.AIR) {
				registerItem(advancementIdentifier, sourceBlockItem, targetBlockItem);
			}
		}
		
		BLOCK_STATE_REGISTRY.put(sourceBlockState, targetBlockState);
		BLOCK_ADVANCEMENT_REGISTRY.put(sourceBlockState, advancementIdentifier);
	}
	
	private static void registerBlockTranslation(Block sourceBlock, MutableText targetTranslation) {
		ALTERNATE_BLOCK_TRANSLATION_STRING_REGISTRY.put(sourceBlock, targetTranslation);
	}
	
	public static boolean hasCloak(BlockState blockState) {
		return BLOCK_STATE_REGISTRY.containsKey(blockState);
	}
	
	public static boolean isVisibleTo(BlockState state, PlayerEntity player) {
		return AdvancementHelper.hasAdvancement(player, BLOCK_ADVANCEMENT_REGISTRY.getOrDefault(state, null));
	}
	
	public static @NotNull Collection<BlockState> getRevealedBlockStates(Identifier advancement) {
		List<BlockState> blockStates = new ArrayList<>();
		if (ADVANCEMENT_BLOCK_REGISTRY.containsKey(advancement)) {
			for (Object entry : ADVANCEMENT_BLOCK_REGISTRY.get(advancement)) {
				if (entry instanceof BlockState blockState) {
					blockStates.add(blockState);
				}
			}
		}
		return blockStates;
	}
	
	public static Map<Identifier, List<BlockState>> getBlockStateEntries() {
		return ADVANCEMENT_BLOCK_REGISTRY;
	}
	
	public static List<BlockState> getBlockStateEntries(Identifier advancement) {
		return ADVANCEMENT_BLOCK_REGISTRY.getOrDefault(advancement, Collections.EMPTY_LIST);
	}
	
	public static List<Block> getBlockEntries() {
		List<Block> blocks = new ArrayList<>();
		for (List<BlockState> states : ADVANCEMENT_BLOCK_REGISTRY.values()) {
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
		if (ADVANCEMENT_BLOCK_REGISTRY.containsKey(advancement)) {
			List<BlockState> states = ADVANCEMENT_BLOCK_REGISTRY.get(advancement);
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
		if (ADVANCEMENT_ITEM_REGISTRY.containsKey(advancementIdentifier)) {
			List<Item> list = ADVANCEMENT_ITEM_REGISTRY.get(advancementIdentifier);
			if (list.contains(sourceItem)) {
				return;
			}
			list.add(sourceItem);
		} else {
			List<Item> list = new ArrayList<>();
			list.add(sourceItem);
			ADVANCEMENT_ITEM_REGISTRY.put(advancementIdentifier, list);
		}
		ITEM_REGISTRY.put(sourceItem, targetItem);
		ITEM_ADVANCEMENT_REGISTRY.put(sourceItem, advancementIdentifier);
	}
	
	private static void registerItemTranslation(Item sourceItem, MutableText targetTranslation) {
		ALTERNATE_ITEM_TRANSLATION_STRING_REGISTRY.put(sourceItem, targetTranslation);
	}
	
	public static boolean hasCloak(Item item) {
		return ITEM_REGISTRY.containsKey(item);
	}
	
	@Nullable
	public static Item getCloak(Item item) {
		return ITEM_REGISTRY.getOrDefault(item, null);
	}
	
	public static boolean isVisibleTo(Item item, PlayerEntity player) {
		return AdvancementHelper.hasAdvancement(player, ITEM_ADVANCEMENT_REGISTRY.getOrDefault(item, null));
	}
	
	@Nullable
	public static BlockState getCloak(BlockState blockState) {
		return BLOCK_STATE_REGISTRY.getOrDefault(blockState, null);
	}
	
	public static @NotNull Collection<Item> getRevealedItems(Identifier advancement) {
		List<Item> items = new ArrayList<>();
		if (ADVANCEMENT_ITEM_REGISTRY.containsKey(advancement)) {
			for (Object entry : ADVANCEMENT_ITEM_REGISTRY.get(advancement)) {
				if (entry instanceof Item item) {
					items.add(item);
				}
			}
		}
		return items;
	}
	
	public static Map<Identifier, List<Item>> getItemEntries() {
		return ADVANCEMENT_ITEM_REGISTRY;
	}
	
	public static List<Item> getItemEntries(Identifier advancement) {
		return ADVANCEMENT_ITEM_REGISTRY.getOrDefault(advancement, Collections.EMPTY_LIST);
	}
	
	public static void write(PacketByteBuf buf) {
		// Block States
		buf.writeInt(ADVANCEMENT_BLOCK_REGISTRY.size());
		for (Map.Entry<Identifier, List<BlockState>> advancementBlocks : ADVANCEMENT_BLOCK_REGISTRY.entrySet()) {
			buf.writeIdentifier(advancementBlocks.getKey());
			buf.writeInt(advancementBlocks.getValue().size());
			for (BlockState blockState : advancementBlocks.getValue()) {
				buf.writeString(BlockArgumentParser.stringifyBlockState(blockState));
				buf.writeString(BlockArgumentParser.stringifyBlockState(BLOCK_STATE_REGISTRY.get(blockState)));
			}
		}
		
		// Items
		buf.writeInt(ADVANCEMENT_ITEM_REGISTRY.size());
		for (Map.Entry<Identifier, List<Item>> advancementItems : ADVANCEMENT_ITEM_REGISTRY.entrySet()) {
			buf.writeIdentifier(advancementItems.getKey());
			buf.writeInt(advancementItems.getValue().size());
			for (Item item : advancementItems.getValue()) {
				buf.writeString(Registry.ITEM.getId(item).toString());
				buf.writeString(Registry.ITEM.getId(ITEM_REGISTRY.get(item)).toString());
			}
		}
		
		// Block Translations
		buf.writeInt(ALTERNATE_BLOCK_TRANSLATION_STRING_REGISTRY.size());
		for (Map.Entry<Block, MutableText> blockTranslation : ALTERNATE_BLOCK_TRANSLATION_STRING_REGISTRY.entrySet()) {
			buf.writeIdentifier(Registry.BLOCK.getId(blockTranslation.getKey()));
			buf.writeText(blockTranslation.getValue());
		}
		
		// Item Translations
		buf.writeInt(ALTERNATE_ITEM_TRANSLATION_STRING_REGISTRY.size());
		for (Map.Entry<Item, MutableText> itemTranslation : ALTERNATE_ITEM_TRANSLATION_STRING_REGISTRY.entrySet()) {
			buf.writeIdentifier(Registry.ITEM.getId(itemTranslation.getKey()));
			buf.writeText(itemTranslation.getValue());
		}
		
	}
	
	public static void fromPacket(PacketByteBuf buf) throws CommandSyntaxException {
		RevelationRegistry.clear();
		RevelationRegistry.addRevelationAwares();
		
		// Block States
		int blockEntries = buf.readInt();
		for (int i = 0; i < blockEntries; i++) {
			Identifier advancementIdentifier = buf.readIdentifier();
			int blockStateCount = buf.readInt();
			for (int j = 0; j < blockStateCount; j++) {
				BlockState sourceState = new BlockArgumentParser(new StringReader(buf.readString()), false).parse(false).getBlockState();
				BlockState targetState = new BlockArgumentParser(new StringReader(buf.readString()), false).parse(false).getBlockState();
				
				if (ADVANCEMENT_BLOCK_REGISTRY.containsKey(advancementIdentifier)) {
					List<BlockState> advancementStates = ADVANCEMENT_BLOCK_REGISTRY.get(advancementIdentifier);
					advancementStates.add(sourceState);
				} else {
					List<BlockState> advancementStates = new ArrayList<>();
					advancementStates.add(sourceState);
					ADVANCEMENT_BLOCK_REGISTRY.put(advancementIdentifier, advancementStates);
				}
				BLOCK_ADVANCEMENT_REGISTRY.put(sourceState, advancementIdentifier);
				BLOCK_STATE_REGISTRY.put(sourceState, targetState);
			}
		}
		
		// Items
		int itemEntries = buf.readInt();
		for (int i = 0; i < itemEntries; i++) {
			Identifier advancementIdentifier = buf.readIdentifier();
			int itemCount = buf.readInt();
			for (int j = 0; j < itemCount; j++) {
				Identifier sourceId = Identifier.tryParse(buf.readString());
				Identifier targetId = Identifier.tryParse(buf.readString());
				Item sourceItem = Registry.ITEM.get(sourceId);
				Item targetItem = Registry.ITEM.get(targetId);
				
				if (ADVANCEMENT_ITEM_REGISTRY.containsKey(advancementIdentifier)) {
					List<Item> advancementItems = ADVANCEMENT_ITEM_REGISTRY.get(advancementIdentifier);
					advancementItems.add(sourceItem);
				} else {
					List<Item> advancementItems = new ArrayList<>();
					advancementItems.add(sourceItem);
					ADVANCEMENT_ITEM_REGISTRY.put(advancementIdentifier, advancementItems);
				}
				ITEM_ADVANCEMENT_REGISTRY.put(sourceItem, advancementIdentifier);
				ITEM_REGISTRY.put(sourceItem, targetItem);
			}
		}
		
		// Block Translations
		int blockTranslations = buf.readInt();
		for (int i = 0; i < blockTranslations; i++) {
			Block block = Registry.BLOCK.get(buf.readIdentifier());
			MutableText text = (MutableText) buf.readText();
			ALTERNATE_BLOCK_TRANSLATION_STRING_REGISTRY.put(block, text);
		}
		
		// Item Translations
		int itemTranslations = buf.readInt();
		for (int i = 0; i < itemTranslations; i++) {
			Item item = Registry.ITEM.get(buf.readIdentifier());
			MutableText text = (MutableText) buf.readText();
			ALTERNATE_ITEM_TRANSLATION_STRING_REGISTRY.put(item, text);
		}
	}
	
}