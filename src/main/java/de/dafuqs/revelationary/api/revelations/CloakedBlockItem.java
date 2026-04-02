package de.dafuqs.revelationary.api.revelations;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CloakedBlockItem extends BlockItem implements RevelationAware {
	Identifier cloakAdvancementIdentifier;
	BlockItem cloakItem;
	
	public CloakedBlockItem(Block block, Properties settings, Identifier cloakAdvancementIdentifier, BlockItem cloakItem) {
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
		return Map.of(this.getBlock().defaultBlockState(), this.cloakItem.getBlock().defaultBlockState());
	}
	
	@Override
	public Tuple<Item, Item> getItemCloak() {
		return new Tuple<>(this, cloakItem);
	}
}
