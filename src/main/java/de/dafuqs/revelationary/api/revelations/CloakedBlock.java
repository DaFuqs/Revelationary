package de.dafuqs.revelationary.api.revelations;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.Pair;

import java.util.Hashtable;
import java.util.Map;

public abstract class CloakedBlock extends Block implements RevelationAware {
	
	final Block cloakedBlock;
	
	public CloakedBlock(Settings settings, Block cloakedBlock) {
		super(settings);
		this.cloakedBlock = cloakedBlock;
		RevelationAware.register(this);
	}
	
	@Override
	public Map<BlockState, BlockState> getBlockStateCloaks() {
		Hashtable<BlockState, BlockState> hashtable = new Hashtable<>();
		hashtable.put(this.getDefaultState(), cloakedBlock.getDefaultState());
		return hashtable;
	}
	
	@Override
	public Pair<Item, Item> getItemCloak() {
		return null;
	}
	
}
