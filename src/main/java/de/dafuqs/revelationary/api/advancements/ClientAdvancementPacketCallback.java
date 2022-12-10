package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.ClientAdvancements;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * Utility interface for listening to advancement sync packets
 * Instead of mixin into AdvancementS2CPacket directly this contains
 * simple full featured lists of advancements that were gotten and removed
 */

@Environment(EnvType.CLIENT)
public interface ClientAdvancementPacketCallback {
	
	/**
	 * Gets called every time advancements get synched from server- to client side
	 *
	 * @param gottenAdvancements  Advancements that the player got
	 * @param removedAdvancements Advancements that the player lost (like via /advancements remove ...)
	 * @param isJoinPacket        True if the trigger is because of the first advancement packet after world join
	 *                            (synching from all the advancements of previous play sessions)
	 */
	void onClientAdvancementPacket(Set<Identifier> gottenAdvancements, Set<Identifier> removedAdvancements, boolean isJoinPacket);
	
	/**
	 * Register a ClientAdvancementPacketCallback so it will receive triggers
	 */
	static void registerCallback(ClientAdvancementPacketCallback callback) {
		ClientAdvancements.callbacks.add(callback);
	}
	
}