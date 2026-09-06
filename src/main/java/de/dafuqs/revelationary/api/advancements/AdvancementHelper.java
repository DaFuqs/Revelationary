package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.*;
import net.minecraft.advancements.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.server.players.*;
import net.minecraft.world.entity.player.*;
import net.neoforged.neoforge.common.util.*;

import java.util.*;

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
	 * @return weather or not the player has the advancement with the given identifier. If the player is a fake player, returns the state of its owner
	 */
	public static boolean hasAdvancement(Player playerEntity, ResourceLocation advancementIdentifier) {
		if (playerEntity == null) {
			return false;
		} else if (advancementIdentifier == null) {
			return true;
		}
		
		if (playerEntity instanceof ServerPlayer serverPlayerEntity) {
			AdvancementHolder advancement = serverPlayerEntity.server.getAdvancements().get(advancementIdentifier);
			if (advancement == null) {
				Revelationary.logError("Player " + playerEntity.getName() + " was getting an advancement check for an advancement that does not exist: " + advancementIdentifier);
				return false;
			} else if(playerEntity instanceof FakePlayer fakePlayer && fakePlayer.getServer() != null) {
				PlayerList playerList = fakePlayer.getServer().getPlayerList();

				// since https://github.com/neoforged/NeoForge/pull/3260/ Neo fake players do not hold advancements anymore, breaking this simple & performant check
				// PlayerAdvancements ownerPlayerAdvancements = playerList.getPlayerAdvancements(fakePlayer.getUUID());
				// return ownerPlayerAdvancements.getOrStartProgress(advancement).isDone();

				// it is good practice to use the owners UUID as the fake players uuid, so this is what we check now
				// if the fake players owner is not online... tough luck
				UUID uuid = fakePlayer.getGameProfile().getId();
				ServerPlayer serverPlayer = playerList.getPlayer(uuid);
				if (serverPlayer == null) {
					return false;
				}
				return playerList.getPlayerAdvancements(serverPlayer).getOrStartProgress(advancement).isDone();
			} else {
				return serverPlayerEntity.getAdvancements().getOrStartProgress(advancement).isDone();
			}
			// we cannot test for "net.minecraft.client.network.ClientPlayerEntity" there because that will get obfuscated
			// to "net.minecraft.class_xxxxx" in compiled versions => works in dev env, breaks in prod
		} else if (playerEntity.getClass().getCanonicalName().startsWith("net.minecraft")) {
			return hasAdvancementClient(advancementIdentifier);
		}

		// Kibe's FakePlayerEntity is neither is a ServerPlayerEntity, nor a ClientPlayerEntity
		return false;
	}
	
	/**
	 * Checks, if the current client player has an advancement
	 *
	 * @param advancementIdentifier the identifier of the advancement to check
	 * @return if the client player has the advancement
	 */
	public static boolean hasAdvancementClient(ResourceLocation advancementIdentifier) {
		return ClientAdvancements.hasDone(advancementIdentifier);
	}
}