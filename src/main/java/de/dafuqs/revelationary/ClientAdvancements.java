package de.dafuqs.revelationary;

import de.dafuqs.revelationary.api.advancements.*;
import de.dafuqs.revelationary.mixin.client.*;
import net.fabricmc.api.*;
import net.minecraft.advancement.*;
import net.minecraft.client.*;
import net.minecraft.client.network.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ClientAdvancements {
	protected static boolean receivedFirstAdvancementPacket = false;
	public static List<ClientAdvancementPacketCallback> callbacks = new ArrayList<>();
	
	public static void onClientPacket(@NotNull AdvancementUpdateS2CPacket packet) {
		boolean hadPacketBefore = receivedFirstAdvancementPacket;
		receivedFirstAdvancementPacket = true;
		boolean isReset = packet.shouldClearCurrent();
		boolean isFirstPacket = !hadPacketBefore || isReset;
		
		Set<Identifier> doneAdvancements = getDoneAdvancements(packet);
		Set<Identifier> removedAdvancements = packet.getAdvancementIdsToRemove();
		
		ClientRevelationHolder.processRemovedAdvancements(removedAdvancements);
		ClientRevelationHolder.processNewAdvancements(doneAdvancements, isFirstPacket);
		
		for (ClientAdvancementPacketCallback callback : callbacks) {
			callback.onClientAdvancementPacket(doneAdvancements, removedAdvancements, isFirstPacket);
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
				PlacedAdvancement adv = cm.getManager().get(identifier);
				if (adv != null) {
					Map<AdvancementEntry, AdvancementProgress> progressMap = ((AccessorClientAdvancementManager) cm).getAdvancementProgresses();
					AdvancementProgress progress = progressMap.get(adv.getAdvancementEntry());
					return progress != null && progress.isDone();
				}
			}
		}
		return false;
	}
	
	public static @NotNull Set<Identifier> getDoneAdvancements(@NotNull AdvancementUpdateS2CPacket packet) {
		Set<Identifier> doneAdvancements = new HashSet<>();
		
		for (Map.Entry<Identifier, AdvancementProgress> progressedAdvancement : packet.getAdvancementsToProgress().entrySet()) {
			if (progressedAdvancement.getValue().isDone()) {
				doneAdvancements.add(progressedAdvancement.getKey());
			}
		}
		
		return doneAdvancements;
	}
	
	public static void playerLogout() {
		receivedFirstAdvancementPacket = false;
	}
}