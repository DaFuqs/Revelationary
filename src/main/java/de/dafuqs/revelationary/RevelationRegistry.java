package de.dafuqs.revelationary;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import de.dafuqs.revelationary.api.advancements.AdvancementHelper;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
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

    public static void clear() {
        ADVANCEMENT_BLOCK_REGISTRY.clear();
        ADVANCEMENT_ITEM_REGISTRY.clear();
        BLOCK_STATE_REGISTRY.clear();
        ITEM_REGISTRY.clear();
    }
    
    private static final Set<RevelationAware> notedRevelationAwares = new HashSet<>();
    public static void registerRevelationAware(RevelationAware revelationAware) {
        notedRevelationAwares.add(revelationAware);
    }
    
    public static void addRevelationAwares() {
        for(RevelationAware revelationAware : notedRevelationAwares) {
            Identifier advancementIdentifier = revelationAware.getCloakAdvancementIdentifier();
            for (Map.Entry<BlockState, BlockState> states : revelationAware.getBlockStateCloaks().entrySet()) {
                registerBlockState(advancementIdentifier, states.getKey(), states.getValue());
            }
            Pair<Item, Item> item = revelationAware.getItemCloak();
            if (item != null) {
                registerItem(advancementIdentifier, item.getLeft(), item.getRight());
            }
        }
        
        notedRevelationAwares.clear();
    }
    
    public static void registerFromJson(JsonObject jsonObject) {
        Identifier advancementIdentifier = Identifier.tryParse(JsonHelper.getString(jsonObject, "advancement"));
        
        for(Map.Entry<String, JsonElement> stateEntry : jsonObject.get("block_states").getAsJsonObject().entrySet()) {
            try {
                BlockState sourceBlockState = new BlockArgumentParser(new StringReader(stateEntry.getKey()), true).parse(false).getBlockState();
                BlockState targetBlockState = new BlockArgumentParser(new StringReader(stateEntry.getValue().getAsString()), true).parse(false).getBlockState();
    
                registerBlockState(advancementIdentifier, sourceBlockState, targetBlockState);
            } catch (Exception e) {
                Revelationary.logError("Error parsing block state: " + e);
            }
        }
        for(Map.Entry<String, JsonElement> itemEntry : jsonObject.get("items").getAsJsonObject().entrySet()) {
            Identifier sourceId = Identifier.tryParse(itemEntry.getKey());
            Identifier targetId = Identifier.tryParse(itemEntry.getValue().getAsString());
            
            Item sourceItem = Registry.ITEM.get(sourceId);
            Item targetItem = Registry.ITEM.get(targetId);
    
            registerItem(advancementIdentifier, sourceItem, targetItem);
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
        if(sourceBlockItem != Items.AIR) {
            Item targetBlockItem = targetBlockState.getBlock().asItem();
            if(targetBlockItem != Items.AIR) {
                registerItem(advancementIdentifier, sourceBlockItem, targetBlockItem);
            }
        }
        
        BLOCK_STATE_REGISTRY.put(sourceBlockState, targetBlockState);
        BLOCK_ADVANCEMENT_REGISTRY.put(sourceBlockState, advancementIdentifier);
    }
    
    public static boolean hasCloak(BlockState blockState) {
        return BLOCK_STATE_REGISTRY.containsKey(blockState);
    }
    
    public static boolean isVisibleTo(BlockState state, PlayerEntity player) {
        return AdvancementHelper.hasAdvancement(player, BLOCK_ADVANCEMENT_REGISTRY.getOrDefault(state, null));
    }
    
    public static @NotNull Collection<BlockState> getRevealedBlockStates(Identifier advancement) {
        List<BlockState> blockStates = new ArrayList<>();
        if(ADVANCEMENT_BLOCK_REGISTRY.containsKey(advancement)) {
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
        for(List<BlockState> states : ADVANCEMENT_BLOCK_REGISTRY.values()) {
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
        if(ADVANCEMENT_BLOCK_REGISTRY.containsKey(advancement)) {
            List<BlockState> states = ADVANCEMENT_BLOCK_REGISTRY.get(advancement);
            List<Block> blocks = new ArrayList<>();
            for(BlockState state : states) {
                Block block = state.getBlock();
                if(!blocks.contains(block)) {
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
        if(ADVANCEMENT_ITEM_REGISTRY.containsKey(advancementIdentifier)) {
            List<Item> list = ADVANCEMENT_ITEM_REGISTRY.get(advancementIdentifier);
            if(list.contains(sourceItem)) {
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
        if(ADVANCEMENT_ITEM_REGISTRY.containsKey(advancement)) {
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

}