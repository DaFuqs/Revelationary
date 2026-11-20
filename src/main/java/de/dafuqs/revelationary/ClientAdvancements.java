package de.dafuqs.revelationary;

import de.dafuqs.revelationary.api.advancements.ClientAdvancementPacketCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ClientAdvancements {
	protected static boolean receivedFirstAdvancementPacket = false;
	public static List<ClientAdvancementPacketCallback> callbacks = new ArrayList<>();
	
	public static void onClientPacket(@NotNull ClientboundUpdateAdvancementsPacket packet) {
		boolean hadPacketBefore = receivedFirstAdvancementPacket;
		receivedFirstAdvancementPacket = true;
		boolean isReset = packet.shouldReset();
		boolean isFirstPacket = !hadPacketBefore || isReset;
		
		Set<Identifier> doneAdvancements = getDoneAdvancements(packet);
		Set<Identifier> removedAdvancements = packet.removed();
		
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
			ClientPacketListener conn = Minecraft.getInstance().getConnection();
			if (conn != null) {
				net.minecraft.client.multiplayer.ClientAdvancements cm = conn.getAdvancements();
				AdvancementHolder adv = cm.get(identifier);
				if (adv != null) {
					AdvancementProgress progress = cm.progress().get(adv);
					return progress != null && progress.isDone();
				}
			}
		}
		return false;
	}
	
	public static @NotNull Set<Identifier> getDoneAdvancements(@NotNull ClientboundUpdateAdvancementsPacket packet) {
		Set<Identifier> doneAdvancements = new HashSet<>();
		
		for (ClientboundUpdateAdvancementsPacket.PositionedAdvancement earnedAdvancementEntry : packet.added()) {
			doneAdvancements.add(earnedAdvancementEntry.advancement().id());
		}
		for (Map.Entry<Identifier, AdvancementProgress> progressedAdvancement : packet.progress().entrySet()) {
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