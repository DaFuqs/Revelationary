package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.ClientAdvancements;
import de.dafuqs.revelationary.Revelationary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class AdvancementHelper {
	
	/**
	 * Checks if any player has the advancement. Can be used both server- and clientside
	 * Special cases:
	 * - if advancementIdentifier is null always returns true
	 * - if playerEntity is null or the advancement does not exist always returns false
	 * - if playerEntity is a fake player always returns false
	 *
	 * @param playerEntity          the player
	 * @param advancementIdentifier the advancement identifier
	 * @return weather or not the player has the advancement with the given identifier
	 */
	public static boolean hasAdvancement(PlayerEntity playerEntity, Identifier advancementIdentifier) {
		if (playerEntity == null) {
			return false;
		} else if (advancementIdentifier == null) {
			return true;
		}
		
		if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
			Advancement advancement = serverPlayerEntity.server.getAdvancementLoader().get(advancementIdentifier);
			if (advancement == null) {
				Revelationary.logError("Player " + playerEntity.getName() + " was getting an advancement check for an advancement that does not exist: " + advancementIdentifier);
				return false;
			} else {
				return serverPlayerEntity.getAdvancementTracker().getProgress(advancement).isDone();
			}
			// we cannot test for "net.minecraft.client.network.ClientPlayerEntity" there because that will get obfuscated
			// to "net.minecraft.class_xxxxx" in compiled versions => works in dev env, breaks in prod
		} else if (playerEntity.getClass().getCanonicalName().startsWith("net.minecraft")) {
			return hasAdvancementClient(advancementIdentifier);
		} else {
			// Kibe's FakePlayerEntity is neither is a ServerPlayerEntity, nor a ClientPlayerEntity
			return false;
		}
	}
	
	/**
	 * Checks, if the current client player has an advancement
	 *
	 * @param advancementIdentifier the identifier of the advancement to check
	 * @return if the client player has the advancement
	 */
	@Environment(EnvType.CLIENT)
	public static boolean hasAdvancementClient(Identifier advancementIdentifier) {
		return ClientAdvancements.hasDone(advancementIdentifier);
	}
	
}