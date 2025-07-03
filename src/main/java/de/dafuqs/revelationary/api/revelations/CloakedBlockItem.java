package de.dafuqs.revelationary.api.revelations;

import net.minecraft.resources.*;
import net.minecraft.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;

import java.util.*;

public class CloakedBlockItem extends BlockItem implements RevelationAware {
	ResourceLocation cloakAdvancementIdentifier;
	BlockItem cloakItem;
	
	public CloakedBlockItem(Block block, Properties settings, ResourceLocation cloakAdvancementIdentifier, BlockItem cloakItem) {
		super(block, settings);
		this.cloakAdvancementIdentifier = cloakAdvancementIdentifier;
		this.cloakItem = cloakItem;
		
		RevelationAware.register(this);
	}
	
	@Override
	public ResourceLocation getCloakAdvancementIdentifier() {
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
