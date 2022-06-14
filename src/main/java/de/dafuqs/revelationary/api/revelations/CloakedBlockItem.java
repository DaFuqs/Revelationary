package de.dafuqs.revelationary.api.revelations;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.Map;

public class CloakedBlockItem extends BlockItem implements RevelationAware {
	
	Identifier cloakAdvancementIdentifier;
	BlockItem cloakItem;
	
	public CloakedBlockItem(Block block, Settings settings, Identifier cloakAdvancementIdentifier, BlockItem cloakItem) {
		super(block, settings);
		this.cloakAdvancementIdentifier = cloakAdvancementIdentifier;
		this.cloakItem = cloakItem;
		
		RevelationAware.register(this);
	}
	
	@Override
	public Identifier getCloakAdvancementIdentifier() {
		return cloakAdvancementIdentifier;
	}
	
	@Override
	public Map<BlockState, BlockState> getBlockStateCloaks() {
		return Map.of(this.getBlock().getDefaultState(), this.cloakItem.getBlock().getDefaultState());
	}
	
	@Override
	public Pair<Item, Item> getItemCloak() {
		return new Pair<>(this, cloakItem);
	}
	
}
