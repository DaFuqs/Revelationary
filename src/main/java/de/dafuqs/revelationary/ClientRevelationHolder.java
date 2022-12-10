package de.dafuqs.revelationary;

import de.dafuqs.revelationary.api.revelations.RevealingCallback;
import de.dafuqs.revelationary.api.revelations.RevelationAware;
import de.dafuqs.revelationary.api.revelations.WorldRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ClientRevelationHolder {
	
	public static List<RevealingCallback> callbacks = new ArrayList<>();
	
	private static final Set<BlockState> activeBlockStateSwaps = new HashSet<>();
	private static final Set<Item> activeItemSwaps = new HashSet<>();
	
	public static void processNewAdvancements(Set<Identifier> doneAdvancements, boolean isJoinPacket) {
		if (!doneAdvancements.isEmpty()) {
			Set<Item> revealedItems = new HashSet<>();
			Set<BlockState> revealedBlockStates = new HashSet<>();
			Set<Block> revealedBlocks = new HashSet<>();
			for (Identifier doneAdvancement : doneAdvancements) {
				revealedItems.addAll(RevelationRegistry.getRevealedItems(doneAdvancement));
				revealedBlockStates.addAll(RevelationRegistry.getRevealedBlockStates(doneAdvancement));
				for (BlockState state : revealedBlockStates) {
					Block block = state.getBlock();
					revealedBlocks.add(block);
				}
			}
			
			if (revealedBlockStates.size() > 0) {
				// uncloak the blocks
				for (BlockState revealedBlockState : revealedBlockStates) {
					activeBlockStateSwaps.remove(revealedBlockState);
					Item blockItem = revealedBlockState.getBlock().asItem();
					if (blockItem != null) {
						activeItemSwaps.remove(blockItem);
					}
				}
				rebuildAllChunks();
			}
			
			for (Block revealedBlock : revealedBlocks) {
				if (revealedBlock instanceof RevelationAware revelationAware) {
					revelationAware.onUncloak();
				}
			}
			for (Item revealedItem : revealedItems) {
				activeItemSwaps.remove(revealedItem);
				if (revealedItem instanceof RevelationAware revelationAware) {
					revelationAware.onUncloak();
				}
			}
			
			if (!revealedBlocks.isEmpty() || !revealedItems.isEmpty()) {
				for (RevealingCallback callback : callbacks) {
					callback.trigger(doneAdvancements, revealedBlocks, revealedItems, isJoinPacket);
				}
			}
		}
	}
	
	public static void processRemovedAdvancements(@NotNull Set<Identifier> removedAdvancements) {
		if (!removedAdvancements.isEmpty()) {
			List<Item> concealedItems = new ArrayList<>();
			List<BlockState> concealedBlockStates = new ArrayList<>();
			List<Block> concealedBlocks = new ArrayList<>();
			for (Identifier removedAdvancement : removedAdvancements) {
				concealedItems.addAll(RevelationRegistry.getRevealedItems(removedAdvancement));
				concealedBlockStates.addAll(RevelationRegistry.getRevealedBlockStates(removedAdvancement));
				for (BlockState state : concealedBlockStates) {
					Block block = state.getBlock();
					if (!concealedBlocks.contains(block)) {
						concealedBlocks.add(block);
					}
				}
			}
			
			if (concealedBlockStates.size() > 0) {
				// uncloak the blocks
				for (BlockState concealedBlockState : concealedBlockStates) {
					if (!activeBlockStateSwaps.contains(concealedBlockState)) {
						activeBlockStateSwaps.add(concealedBlockState);
					}
					Item blockItem = concealedBlockState.getBlock().asItem();
					if (blockItem != null) {
						if (!activeItemSwaps.contains(blockItem)) {
							activeItemSwaps.add(blockItem);
						}
					}
				}
				rebuildAllChunks();
			}
			
			activeItemSwaps.addAll(concealedItems);
			
			for (Block concealedBlock : concealedBlocks) {
				if (concealedBlock instanceof RevelationAware revelationAware) {
					revelationAware.onCloak();
				}
			}
			for (Item concealedItem : concealedItems) {
				if (concealedItem instanceof RevelationAware revelationAware) {
					revelationAware.onCloak();
				}
			}
		}
	}
	
	// rerender chunks to show newly swapped blocks
	static void rebuildAllChunks() {
		WorldRenderer renderer = MinecraftClient.getInstance().worldRenderer;
		((WorldRendererAccessor) renderer).rebuildAllChunks();
	}
	
	// BLOCKS
	private static void cloak(BlockState blockState) {
		activeBlockStateSwaps.add(blockState);
		if (blockState instanceof RevelationAware revelationAware) {
			revelationAware.onCloak();
		}
	}
	
	public static boolean isCloaked(Block block) {
		return activeBlockStateSwaps.contains(block.getDefaultState());
	}
	
	public static boolean isCloaked(BlockState blockState) {
		return activeBlockStateSwaps.contains(blockState);
	}
	
	public static BlockState getCloakTarget(BlockState blockState) {
		if (isCloaked(blockState)) {
			return RevelationRegistry.getCloak(blockState);
		} else {
			return blockState;
		}
	}
	
	// ITEMS
	private static void cloak(Item item) {
		activeItemSwaps.add(item);
		if (item instanceof RevelationAware revelationAware) {
			revelationAware.onCloak();
		}
	}
	
	public static boolean isCloaked(Item item) {
		return activeItemSwaps.contains(item);
	}
	
	public static Item getCloakTarget(Item item) {
		if (isCloaked(item)) {
			return RevelationRegistry.getCloak(item);
		} else {
			return item;
		}
	}
	
	public static void cloakAll() {
		activeItemSwaps.clear();
		activeBlockStateSwaps.clear();
		
		for (List<BlockState> registeredRevelations : RevelationRegistry.getBlockStateEntries().values()) {
			for (BlockState registeredRevelation : registeredRevelations) {
				cloak(registeredRevelation);
			}
		}
		for (List<Item> registeredRevelations : RevelationRegistry.getItemEntries().values()) {
			for (Item registeredRevelation : registeredRevelations) {
				cloak(registeredRevelation);
			}
		}
	}
	
}
