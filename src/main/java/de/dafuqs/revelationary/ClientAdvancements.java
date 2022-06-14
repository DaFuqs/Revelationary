package de.dafuqs.revelationary;

import de.dafuqs.revelationary.api.advancements.ClientAdvancementPacketCallback;
import de.dafuqs.revelationary.mixin.client.AccessorClientAdvancementManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ClientAdvancements {
	
	protected static boolean receivedFirstAdvancementPacket = false;
	protected static List<ClientAdvancementPacketCallback> callbacks = new ArrayList<>();
	
	public static void onClientPacket(@NotNull AdvancementUpdateS2CPacket packet) {
		boolean hadPacketBefore = receivedFirstAdvancementPacket;
		receivedFirstAdvancementPacket = true;
		
		Set<Identifier> doneAdvancements = getDoneAdvancements(packet);
		Set<Identifier> removedAdvancements = packet.getAdvancementIdsToRemove();
		
		ClientRevelationHolder.processRemovedAdvancements(removedAdvancements);
		ClientRevelationHolder.processNewAdvancements(doneAdvancements, !hadPacketBefore);
		
		for(ClientAdvancementPacketCallback callback : callbacks) {
			callback.onClientAdvancementPacket(doneAdvancements, removedAdvancements, !hadPacketBefore);
		}
	}
	
	public static boolean hasDone(Identifier identifier) {
		// If we never received the initial packet: assume false
		if (!receivedFirstAdvancementPacket) {
			return false;
		}
		
		if (identifier != null) {
			ClientPlayNetworkHandler conn = MinecraftClient.getInstance().getNetworkHandler();
			if (conn != null) {
				ClientAdvancementManager cm = conn.getAdvancementHandler();
				Advancement adv = cm.getManager().get(identifier);
				if (adv != null) {
					Map<Advancement, AdvancementProgress> progressMap = ((AccessorClientAdvancementManager) cm).getAdvancementProgresses();
					AdvancementProgress progress = progressMap.get(adv);
					return progress != null && progress.isDone();
				}
			}
		}
		return false;
	}
	
	public static @NotNull Set<Identifier> getDoneAdvancements(@NotNull AdvancementUpdateS2CPacket packet) {
		Set<Identifier> doneAdvancements = new HashSet<>();
		
		for (Identifier earnedAdvancementIdentifier : packet.getAdvancementsToEarn().keySet()) {
			if (ClientAdvancements.hasDone(earnedAdvancementIdentifier)) {
				doneAdvancements.add(earnedAdvancementIdentifier);
			}
		}
		for (Identifier progressedAdvancementIdentifier : packet.getAdvancementsToProgress().keySet()) {
			if (ClientAdvancements.hasDone(progressedAdvancementIdentifier)) {
				doneAdvancements.add(progressedAdvancementIdentifier);
			}
		}
		
		return doneAdvancements;
	}
	
	public static void playerLogout() {
		RevelationRegistry.addRevelationAwares();
		ClientRevelationHolder.cloakAll();
		receivedFirstAdvancementPacket = false;
	}
	
}