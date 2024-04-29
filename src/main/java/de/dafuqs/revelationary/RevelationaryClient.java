package de.dafuqs.revelationary;

import de.dafuqs.revelationary.networking.RevelationaryPackets;
import de.dafuqs.revelationary.networking.RevelationaryS2CPacketReceivers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class RevelationaryClient implements ClientModInitializer {
	
	@Override
	public void onInitializeClient() {
		// note: guarantee clientside initialization strictly before registering the packet receivers
		// PayloadTypeRegistryImpl throws an IllegalArgumentException if the packet has already been registered
		// which is explicitly ignored (other errors are allowed to pass through for proper handling by mod dev)
		try {
			PayloadTypeRegistry.playS2C().register(RevelationaryPackets.RevelationSync.ID, RevelationaryPackets.RevelationSync.CODEC);
		} catch(IllegalArgumentException ignored) {}
		RevelationaryS2CPacketReceivers.register();
	}
	
}
