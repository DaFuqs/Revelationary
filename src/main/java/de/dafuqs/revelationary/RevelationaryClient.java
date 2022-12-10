package de.dafuqs.revelationary;

import de.dafuqs.revelationary.networking.RevelationaryS2CPacketReceivers;
import net.fabricmc.api.ClientModInitializer;

public class RevelationaryClient implements ClientModInitializer {
	
	@Override
	public void onInitializeClient() {
		RevelationaryS2CPacketReceivers.register();
	}
	
}
