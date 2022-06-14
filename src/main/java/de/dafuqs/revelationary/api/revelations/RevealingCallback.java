package de.dafuqs.revelationary.api.revelations;

import de.dafuqs.revelationary.ClientRevelationHolder;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.Set;

public interface RevealingCallback {
	
	void trigger(Set<Identifier> advancements, Set<Block> blocks, Set<Item> items, boolean isJoinPacket);
	
	static void register(RevealingCallback callback) {
		ClientRevelationHolder.callbacks.add(callback);
	}
	
	static void unregister(RevealingCallback callback) {
		ClientRevelationHolder.callbacks.remove(callback);
	}
	
}