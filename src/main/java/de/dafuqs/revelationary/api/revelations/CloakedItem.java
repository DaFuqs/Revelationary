package de.dafuqs.revelationary.api.revelations;

import java.util.Hashtable;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

public class CloakedItem extends Item implements RevelationAware {
	Identifier cloakAdvancementIdentifier;
	Item cloakItem;
	
	public CloakedItem(Properties settings, Identifier cloakAdvancementIdentifier, Item cloakItem) {
		super(settings);
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
		return new Hashtable<>();
	}
	
	@Override
	public Tuple<Item, Item> getItemCloak() {
		return new Tuple<>(this, cloakItem);
	}
}
