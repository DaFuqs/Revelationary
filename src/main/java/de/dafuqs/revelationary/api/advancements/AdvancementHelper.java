package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.Revelationary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class AdvancementHelper {
	
	public static boolean hasAdvancement(PlayerEntity playerEntity, Identifier advancementIdentifier) {
		if (playerEntity == null) {
			return false;
		} else if (advancementIdentifier == null) {
			return true;
		}
		
		if (playerEntity instanceof ServerPlayerEntity) {
			Advancement advancement = Revelationary.minecraftServer.getAdvancementLoader().get(advancementIdentifier);
			if (advancement == null) {
				Revelationary.logError("Player " + playerEntity.getName() + " was getting an advancement check for an advancement that does not exist: " + advancementIdentifier);
				return false;
			} else {
				return ((ServerPlayerEntity) playerEntity).getAdvancementTracker().getProgress(advancement).isDone();
			}
			// we cannot test for "net.minecraft.client.network.ClientPlayerEntity" there because that will get obfuscated
			// to "net.minecraft.class_xxxxx" in compiled versions => works in dev env, breaks in prod
		} else if (playerEntity.getClass().getCanonicalName().startsWith("net.minecraft")) {
			return hasAdvancementClient(advancementIdentifier);
		} else {
			// thank you, Kibe FakePlayerEntity
			// it neither is a ServerPlayerEntity, nor a ClientPlayerEntity
			return false;
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static boolean hasAdvancementClient(Identifier advancementIdentifier) {
		return ClientAdvancements.hasDone(advancementIdentifier);
	}
	
}