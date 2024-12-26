package de.dafuqs.revelationary;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.fabricmc.fabric.api.resource.*;
import net.minecraft.block.*;
import net.minecraft.command.*;
import net.minecraft.command.argument.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.resource.*;
import net.minecraft.resource.featuretoggle.*;
import net.minecraft.server.command.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.profiler.*;

import java.util.*;

public class RevelationDataLoader extends JsonDataLoader<RevelationDataLoader.RevelationEntry> implements IdentifiableResourceReloadListener {
	
	public static final String LOCATION = "revelations";
	public static final Identifier ID = Identifier.of(Revelationary.MOD_ID, LOCATION);
	public static final RevelationDataLoader INSTANCE = new RevelationDataLoader();
	
	private RevelationDataLoader() {
		super(RevelationEntry.CODEC, ResourceFinder.json(LOCATION));
	}
	
	@Override
	protected void apply(Map<Identifier, RevelationEntry> prepared, ResourceManager manager, Profiler profiler) {
		RegistryWrapper.WrapperLookup lookup = BuiltinRegistries.createWrapperLookup();
		RegistryWrapper<Block> blockRegistryWrapper = CommandRegistryAccess.of(lookup, FeatureFlags.FEATURE_MANAGER.getFeatureSet()).getOrThrow(RegistryKeys.BLOCK);;
		
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
	
	public static void registerFromJson(RegistryWrapper<Block> blockRegistryWrapper, RevelationEntry rev) {
		
		for (Map.Entry<String, String> stateEntry : rev.blockStateSwaps.entrySet()) {
			try {
				BlockState sourceBlockState = BlockArgumentParser.block(blockRegistryWrapper, stateEntry.getKey(), false).blockState();
				BlockState targetBlockState = BlockArgumentParser.block(blockRegistryWrapper, stateEntry.getValue(), false).blockState();
				if (sourceBlockState.isAir()) {
					Revelationary.logError("Trying to register invalid block cloak. Advancement: " + rev.advancementId
							+ " Source Block: " + Registries.BLOCK.getId(sourceBlockState.getBlock())
							+ " Target Block: " + Registries.BLOCK.getId(targetBlockState.getBlock()));
				}
				
				RevelationRegistry.registerBlockState(rev.advancementId, sourceBlockState, targetBlockState);
			} catch (Exception e) {
				Revelationary.logError("Error parsing block state: " + e);
			}
		}
		
		for (Map.Entry<String, String> itemEntry : rev.itemSwaps.entrySet()) {
			Identifier sourceId = Identifier.tryParse(itemEntry.getKey());
			Identifier targetId = Identifier.tryParse(itemEntry.getValue());
			
			Item sourceItem = Registries.ITEM.get(sourceId);
			Item targetItem = Registries.ITEM.get(targetId);
			
			RevelationRegistry.registerItem(rev.advancementId, sourceItem, targetItem);
		}
		
		for (Map.Entry<String, String> blockNameEntry : rev.blockTranslations.entrySet()) {
			Identifier sourceId = Identifier.tryParse(blockNameEntry.getKey());
			MutableText targetText = Text.translatable(blockNameEntry.getValue());
			
			Block sourceBlock = Registries.BLOCK.get(sourceId);
			RevelationRegistry.registerBlockTranslation(sourceBlock, targetText);
			
			Item blockItem = sourceBlock.asItem();
			if (blockItem != null && blockItem != Items.AIR) {
				RevelationRegistry.registerItemTranslation(blockItem, targetText);
			}
		}
		
		for (Map.Entry<String, String> itemNameEntry : rev.itemTranslations.entrySet()) {
			Identifier sourceId = Identifier.tryParse(itemNameEntry.getKey());
			MutableText targetText = Text.translatable(itemNameEntry.getValue());
			
			Item sourceItem = Registries.ITEM.get(sourceId);
			RevelationRegistry.registerItemTranslation(sourceItem, targetText);
		}
	}
	
}