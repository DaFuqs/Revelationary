package de.dafuqs.revelationary.api.revelations;

import com.mojang.datafixers.util.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;

import java.util.*;

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
	public Pair<Item, Item> getItemCloak() {
		return new Pair<>(this, cloakItem);
	}
}
