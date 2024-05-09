package de.dafuqs.revelationary.networking;

import de.dafuqs.revelationary.RevelationRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class RevelationaryS2CPacketSenders {
	
	public static void sendRevelations(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, RevelationRegistry.intoPacket());
	}

}
