package de.dafuqs.revelationary;

import net.neoforged.neoforge.network.handling.*;

public class ClientPayloadHandler {
	
	public static void handleDataOnMain(final RevelationaryNetworking.RevelationSync data, final IPayloadContext context) {
		context.enqueueWork(() -> {
					RevelationRegistry.fromPacket(data);
					ClientRevelationHolder.cloakAll();
				})
				.exceptionally(e -> {
					Revelationary.logError("Error fetching results from sync packet");
					Revelationary.logException(e);
					return null;
				});
	}
}