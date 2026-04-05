package de.dafuqs.revelationary;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.fabricmc.fabric.api.resource.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

public class RevelationDataLoader extends SimpleJsonResourceReloadListener<RevelationDataLoader.RevelationEntry> implements IdentifiableResourceReloadListener {
	
	public static final String LOCATION = "revelations";
	public static final Identifier ID = Identifier.fromNamespaceAndPath(Revelationary.MOD_ID, LOCATION);
	public static final RevelationDataLoader INSTANCE = new RevelationDataLoader();
	
	private RevelationDataLoader() {
		super(RevelationEntry.CODEC, FileToIdConverter.json(LOCATION));
	}
	
	@Override
	protected void apply(Map<Identifier, RevelationEntry> prepared, ResourceManager manager, ProfilerFiller profiler) {
		HolderLookup.Provider lookup = VanillaRegistries.createLookup();
		HolderLookup<Block> blockRegistryWrapper = CommandBuildContext.simple(lookup, FeatureFlags.REGISTRY.allFlags()).lookupOrThrow(Registries.BLOCK);;
		
		prepared.forEach((identifier, revelationEntry) -> registerFromJson(blockRegistryWrapper, revelationEntry));
		RevelationRegistry.deepTrim();
	}
	
	@Override
	public Identifier getFabricId() {
		return ID;
	}
	
	public record RevelationEntry(Identifier advancementId, Map<String, String> blockStateSwaps, Map<String, String> itemSwaps, Map<String, String> blockTranslations, Map<String, String> itemTranslations) {
		
		public static final Codec<RevelationEntry> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
				Identifier.CODEC.fieldOf("advancement").forGetter(RevelationEntry::advancementId),
				Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("block_states").forGetter(RevelationEntry::blockStateSwaps),
				Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("items").forGetter(RevelationEntry::itemSwaps),
				Codec.unboundedMap(Codec.STRING, Codec.STRING).lenientOptionalFieldOf("block_name_replacements", Map.of()).forGetter(RevelationEntry::blockTranslations),
				Codec.unboundedMap(Codec.STRING, Codec.STRING).lenientOptionalFieldOf("item_name_replacements", Map.of()).forGetter(RevelationEntry::itemTranslations)
		).apply(instance, RevelationEntry::new));
		
	}
	
	public static void registerFromJson(HolderLookup<Block> blockRegistryWrapper, RevelationEntry rev) {
		
		for (Map.Entry<String, String> stateEntry : rev.blockStateSwaps.entrySet()) {
			try {
				BlockState sourceBlockState = BlockStateParser.parseForBlock(blockRegistryWrapper, stateEntry.getKey(), false).blockState();
				BlockState targetBlockState = BlockStateParser.parseForBlock(blockRegistryWrapper, stateEntry.getValue(), false).blockState();
				if (sourceBlockState.isAir()) {
					Revelationary.logError("Trying to register invalid block cloak. Advancement: " + rev.advancementId
							+ " Source Block: " + BuiltInRegistries.BLOCK.getKey(sourceBlockState.getBlock())
							+ " Target Block: " + BuiltInRegistries.BLOCK.getKey(targetBlockState.getBlock()));
				}
				
				RevelationRegistry.registerBlockState(rev.advancementId, sourceBlockState, targetBlockState);
			} catch (Exception e) {
				Revelationary.logError("Error parsing block state: " + e);
			}
		}
		
		for (Map.Entry<String, String> itemEntry : rev.itemSwaps.entrySet()) {
			Identifier sourceId = Identifier.tryParse(itemEntry.getKey());
			Identifier targetId = Identifier.tryParse(itemEntry.getValue());
			
			Item sourceItem = BuiltInRegistries.ITEM.getValue(sourceId);
			Item targetItem = BuiltInRegistries.ITEM.getValue(targetId);
			
			RevelationRegistry.registerItem(rev.advancementId, sourceItem, targetItem);
		}
		
		for (Map.Entry<String, String> blockNameEntry : rev.blockTranslations.entrySet()) {
			Identifier sourceId = Identifier.tryParse(blockNameEntry.getKey());
			MutableComponent targetText = Component.translatable(blockNameEntry.getValue());
			
			Block sourceBlock = BuiltInRegistries.BLOCK.getValue(sourceId);
			RevelationRegistry.registerBlockTranslation(sourceBlock, targetText);
			
			Item blockItem = sourceBlock.asItem();
			if (blockItem != null && blockItem != Items.AIR) {
				RevelationRegistry.registerItemTranslation(blockItem, targetText);
			}
		}
		
		for (Map.Entry<String, String> itemNameEntry : rev.itemTranslations.entrySet()) {
			Identifier sourceId = Identifier.tryParse(itemNameEntry.getKey());
			MutableComponent targetText = Component.translatable(itemNameEntry.getValue());
			
			Item sourceItem = BuiltInRegistries.ITEM.getValue(sourceId);
			RevelationRegistry.registerItemTranslation(sourceItem, targetText);
		}
	}
	
}