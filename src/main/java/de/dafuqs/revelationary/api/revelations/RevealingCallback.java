package de.dafuqs.revelationary.api.revelations;

import de.dafuqs.revelationary.revelationary.RevelationHolder;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.Set;

public interface RevealingCallback {
	
	void trigger(Set<Identifier> advancements, Set<Block> blocks, Set<Item> items);
	
	static void register(RevealingCallback callback) {
		RevelationHolder.callbacks.add(callback);
	}
	
	static void unregister(RevealingCallback callback) {
		RevelationHolder.callbacks.remove(callback);
	}
	
}