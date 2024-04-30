package de.dafuqs.revelationary.networking;

import de.dafuqs.revelationary.ClientRevelationHolder;
import de.dafuqs.revelationary.RevelationRegistry;
import de.dafuqs.revelationary.Revelationary;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RevelationaryS2CPacketReceivers {
	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(RevelationaryPackets.RevelationSync.ID, (payload, context) -> {
			try {
				RevelationRegistry.fromPacket(payload.bufCopy());
			} catch (Exception e) {
				Revelationary.logError("Error fetching results from sync packet");
				e.printStackTrace();
			}
			ClientRevelationHolder.cloakAll();
		});
	}
}
