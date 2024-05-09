package de.dafuqs.revelationary;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.dafuqs.revelationary.api.advancements.AdvancementHelper;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import de.dafuqs.revelationary.config.RevelationaryConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Language;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RevelationRegistry {
	
	private static final Object2ObjectOpenHashMap<Identifier, List<BlockState>> advToBlockStates = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectOpenHashMap<BlockState, Identifier> blockStateToAdv = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectOpenHashMap<BlockState, BlockState> blockStateCloaks = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectOpenHashMap<Block, Block> blockCloaks = new Object2ObjectOpenHashMap<>();

	private static final Object2ObjectOpenHashMap<Identifier, List<Item>> advToItems = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectOpenHashMap<Item, Identifier> itemToAdv = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectOpenHashMap<Item, Item> itemCloaks = new Object2ObjectOpenHashMap<>();
	
	private static final Object2ObjectOpenHashMap<Block, MutableText> cloakedBlockNameTranslations = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectOpenHashMap<Item, MutableText> cloakedItemNameTranslations = new Object2ObjectOpenHashMap<>();
	
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
	
	public static void clear() {
		advToBlockStates.clear();
		advToItems.clear();
		blockStateCloaks.clear();
		itemCloaks.clear();
		cloakedBlockNameTranslations.clear();
		cloakedItemNameTranslations.clear();
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
					BlockState sourceBlockState = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), stateEntry.getKey(), true).blockState();
					BlockState targetBlockState = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), stateEntry.getValue().getAsString(), true).blockState();
					
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
	private static void registerBlockState(Identifier advancementIdentifier, BlockState sourceBlockState, BlockState targetBlockState) {
		if(sourceBlockState.isAir()) {
			Revelationary.logError("Trying to register invalid block cloak. Advancement: " + advancementIdentifier
					+ " Source Block: " + Registries.BLOCK.getId(sourceBlockState.getBlock())
					+ " Target Block: " + Registries.BLOCK.getId(targetBlockState.getBlock()));
			return;
		}
		
		List<BlockState> list;
		if (advToBlockStates.containsKey(advancementIdentifier)) {
			list = advToBlockStates.get(advancementIdentifier);
			list.add(sourceBlockState);
		} else {
			list = new ArrayList<>();
			list.add(sourceBlockState);
			advToBlockStates.put(advancementIdentifier, list);
		}
		
		Item sourceBlockItem = sourceBlockState.getBlock().asItem();
		if (sourceBlockItem != Items.AIR) {
			Item targetBlockItem = targetBlockState.getBlock().asItem();
			if (targetBlockItem != Items.AIR) {
				registerItem(advancementIdentifier, sourceBlockItem, targetBlockItem);
			}
		}
		
		blockStateCloaks.put(sourceBlockState, targetBlockState);
		blockCloaks.putIfAbsent(sourceBlockState.getBlock(), targetBlockState.getBlock());
		blockStateToAdv.put(sourceBlockState, advancementIdentifier);
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
		return advToBlockStates;
	}
	
	public static List<BlockState> getBlockStateEntries(Identifier advancement) {
		return advToBlockStates.getOrDefault(advancement, Collections.EMPTY_LIST);
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
			List<Item> list = advToItems.get(advancementIdentifier);
			if (list.contains(sourceItem)) {
				return;
			}
			list.add(sourceItem);
		} else {
			List<Item> list = new ArrayList<>();
			list.add(sourceItem);
			advToItems.put(advancementIdentifier, list);
		}
		itemCloaks.put(sourceItem, targetItem);
		itemToAdv.put(sourceItem, advancementIdentifier);
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
		List<Item> items = new ArrayList<>();
		if (advToItems.containsKey(advancement)) {
			for (Object entry : advToItems.get(advancement)) {
				if (entry instanceof Item item) {
					items.add(item);
				}
			}
		}
		return items;
	}
	
	public static Map<Identifier, List<Item>> getItemEntries() {
		return advToItems;
	}
	
	public static List<Item> getItemEntries(Identifier advancement) {
		return advToItems.getOrDefault(advancement, Collections.EMPTY_LIST);
	}

	private static void writeText(PacketByteBuf buf, Text text) {
		TextCodecs.PACKET_CODEC.encode(buf, text);
	}

	private static Text readText(PacketByteBuf buf) {
		return TextCodecs.PACKET_CODEC.decode(buf);
	}
	
	public static void write(PacketByteBuf buf) {
		// Block States
		buf.writeInt(advToBlockStates.size());
		for (Map.Entry<Identifier, List<BlockState>> advancementBlocks : advToBlockStates.entrySet()) {
			buf.writeIdentifier(advancementBlocks.getKey());
			buf.writeInt(advancementBlocks.getValue().size());
			for (BlockState blockState : advancementBlocks.getValue()) {
				buf.writeString(BlockArgumentParser.stringifyBlockState(blockState));
				buf.writeString(BlockArgumentParser.stringifyBlockState(blockStateCloaks.get(blockState)));
			}
		}
		
		// Items
		buf.writeInt(advToItems.size());
		for (Map.Entry<Identifier, List<Item>> advancementItems : advToItems.entrySet()) {
			buf.writeIdentifier(advancementItems.getKey());
			buf.writeInt(advancementItems.getValue().size());
			for (Item item : advancementItems.getValue()) {
				buf.writeString(Registries.ITEM.getId(item).toString());
				buf.writeString(Registries.ITEM.getId(itemCloaks.get(item)).toString());
			}
		}
		
		// Block Translations
		buf.writeInt(cloakedBlockNameTranslations.size());
		for (Map.Entry<Block, MutableText> blockTranslation : cloakedBlockNameTranslations.entrySet()) {
			buf.writeIdentifier(Registries.BLOCK.getId(blockTranslation.getKey()));
			writeText(buf, blockTranslation.getValue());
		}
		
		// Item Translations
		buf.writeInt(cloakedItemNameTranslations.size());
		for (Map.Entry<Item, MutableText> itemTranslation : cloakedItemNameTranslations.entrySet()) {
			buf.writeIdentifier(Registries.ITEM.getId(itemTranslation.getKey()));
			writeText(buf, itemTranslation.getValue());
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
				BlockState sourceState = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), buf.readString(), true).blockState();
				BlockState targetState = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), buf.readString(), true).blockState();
				
				if (advToBlockStates.containsKey(advancementIdentifier)) {
					List<BlockState> advancementStates = advToBlockStates.get(advancementIdentifier);
					advancementStates.add(sourceState);
				} else {
					List<BlockState> advancementStates = new ArrayList<>();
					advancementStates.add(sourceState);
					advToBlockStates.put(advancementIdentifier, advancementStates);
				}
				blockStateToAdv.put(sourceState, advancementIdentifier);
				blockStateCloaks.put(sourceState, targetState);
				blockCloaks.putIfAbsent(sourceState.getBlock(), targetState.getBlock());
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
				Item sourceItem = Registries.ITEM.get(sourceId);
				Item targetItem = Registries.ITEM.get(targetId);
				
				if (advToItems.containsKey(advancementIdentifier)) {
					List<Item> advancementItems = advToItems.get(advancementIdentifier);
					advancementItems.add(sourceItem);
				} else {
					List<Item> advancementItems = new ArrayList<>();
					advancementItems.add(sourceItem);
					advToItems.put(advancementIdentifier, advancementItems);
				}
				itemToAdv.put(sourceItem, advancementIdentifier);
				itemCloaks.put(sourceItem, targetItem);
			}
		}
		
		// Block Translations
		int blockTranslations = buf.readInt();
		for (int i = 0; i < blockTranslations; i++) {
			Block block = Registries.BLOCK.get(buf.readIdentifier());
			MutableText text = (MutableText) readText(buf);
			cloakedBlockNameTranslations.put(block, text);
		}
		
		// Item Translations
		int itemTranslations = buf.readInt();
		for (int i = 0; i < itemTranslations; i++) {
			Item item = Registries.ITEM.get(buf.readIdentifier());
			MutableText text = (MutableText) readText(buf);
			cloakedItemNameTranslations.put(item, text);
		}
	}
	
}