package de.dafuqs.revelationary;

import de.dafuqs.revelationary.api.advancements.AdvancementCriteria;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Revelationary implements ModInitializer {
	
	public static final String MOD_ID = "revelationary";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer minecraftServer;
	
	public static void logInfo(String message) {
		LOGGER.info("[Revelationary] " + message);
	}
	
	public static void logWarning(String message) {
		LOGGER.warn("[Revelationary] " + message);
	}
	
	public static void logError(String message) {
		LOGGER.error("[Revelationary] " + message);
	}
	
	@Override
	public void onInitialize() {
		logInfo("Starting Common Startup");
		
		AdvancementCriteria.register();
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(RevelationDataLoader.INSTANCE);
		
		ServerWorldEvents.LOAD.register((minecraftServer, serverWorld) -> {
			Revelationary.minecraftServer = minecraftServer;
		});
		
		logInfo("Common startup completed!");
	}
	
}
