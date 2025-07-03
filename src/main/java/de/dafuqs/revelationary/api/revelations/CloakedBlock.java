package de.dafuqs.revelationary.api.revelations;

import net.minecraft.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;

import java.util.*;

public abstract class CloakedBlock extends Block implements RevelationAware {
	final Block cloakedBlock;
	
	public CloakedBlock(Properties settings, Block cloakedBlock) {
		super(settings);
		this.cloakedBlock = cloakedBlock;
		RevelationAware.register(this);
	}
	
	@Override
	public Map<BlockState, BlockState> getBlockStateCloaks() {
		Hashtable<BlockState, BlockState> hashtable = new Hashtable<>();
		hashtable.put(this.defaultBlockState(), cloakedBlock.defaultBlockState());
		return hashtable;
	}
	
	@Override
	public Tuple<Item, Item> getItemCloak() {
		return null;
	}
}
