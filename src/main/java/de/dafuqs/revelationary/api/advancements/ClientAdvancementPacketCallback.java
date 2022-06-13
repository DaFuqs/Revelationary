package de.dafuqs.revelationary.api.advancements;

import net.minecraft.util.Identifier;

import java.util.Set;

public interface ClientAdvancementPacketCallback {
	
	void onClientAdvancementPacket(Set<Identifier> gottenAdvancements, Set<Identifier> removedAdvancements, boolean isJoinPacket);
	
	static void registerCallback(ClientAdvancementPacketCallback callback) {
		ClientAdvancements.callbacks.add(callback);
	}
	
}
