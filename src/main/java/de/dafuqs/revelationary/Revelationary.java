package de.dafuqs.revelationary;

import de.dafuqs.revelationary.api.advancements.*;
import de.dafuqs.revelationary.config.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.resource.*;
import net.fabricmc.loader.api.*;
import net.minecraft.resource.*;
import net.minecraft.server.*;
import org.slf4j.*;

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

        RevelationaryConfig.load();
        AdvancementCriteria.register();
        Commands.register();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(RevelationDataLoader.INSTANCE);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Revelationary.minecraftServer = server;
        });

        if (FabricLoader.getInstance().isModLoaded("sodium")) {
            logWarning("Sodium detected. Chunk rebuilding will be done in cursed mode.");
        }

        logInfo("Common startup completed!");
    }

}
