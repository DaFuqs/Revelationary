package de.dafuqs.revelationary;

import de.dafuqs.revelationary.api.advancements.*;
import de.dafuqs.revelationary.mixin.client.*;
import net.minecraft.advancements.*;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ClientAdvancements {
	protected static boolean receivedFirstAdvancementPacket = false;
	public static List<ClientAdvancementPacketCallback> callbacks = new ArrayList<>();
	
	public static void onClientPacket(@NotNull ClientboundUpdateAdvancementsPacket packet) {
		boolean hadPacketBefore = receivedFirstAdvancementPacket;
		receivedFirstAdvancementPacket = true;
		boolean isReset = packet.shouldReset();
		boolean isFirstPacket = !hadPacketBefore || isReset;
		
		Set<ResourceLocation> doneAdvancements = getDoneAdvancements(packet);
		Set<ResourceLocation> removedAdvancements = packet.getRemoved();
		
		ClientRevelationHolder.processRemovedAdvancements(removedAdvancements);
		ClientRevelationHolder.processNewAdvancements(doneAdvancements, isFirstPacket);
		
		for (ClientAdvancementPacketCallback callback : callbacks) {
			callback.onClientAdvancementPacket(doneAdvancements, removedAdvancements, isFirstPacket);
		}
	}
	
	public static boolean hasDone(ResourceLocation identifier) {
		// If we never received the initial packet: assume false
		if (!receivedFirstAdvancementPacket) {
			return false;
		}
		
		if (identifier != null) {
			ClientPacketListener conn = Minecraft.getInstance().getConnection();
			if (conn != null) {
				net.minecraft.client.multiplayer.ClientAdvancements cm = conn.getAdvancements();
				AdvancementNode adv = cm.getTree().get(identifier);
				if (adv != null) {
					Map<AdvancementHolder, AdvancementProgress> progressMap = ((AccessorClientAdvancementManager) cm).getProgress();
					AdvancementProgress progress = progressMap.get(adv.holder());
					return progress != null && progress.isDone();
				}
			}
		}
		return false;
	}
	
	public static @NotNull Set<ResourceLocation> getDoneAdvancements(@NotNull ClientboundUpdateAdvancementsPacket packet) {
		Set<ResourceLocation> doneAdvancements = new HashSet<>();
		
		for (Map.Entry<ResourceLocation, AdvancementProgress> progressedAdvancement : packet.getProgress().entrySet()) {
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