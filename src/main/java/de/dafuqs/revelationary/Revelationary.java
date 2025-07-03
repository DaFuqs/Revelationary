package de.dafuqs.revelationary;

import de.dafuqs.revelationary.api.advancements.*;
import net.minecraft.server.level.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.loading.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.server.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

@Mod(Revelationary.MOD_ID)
public class Revelationary {
    
    public static final String MOD_ID = "revelationary";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void logInfo(String message) {
        LOGGER.info("[Revelationary] {}", message);
    }

    public static void logWarning(String message) {
        LOGGER.warn("[Revelationary] {}", message);
    }

    public static void logError(String message) {
        LOGGER.error("[Revelationary] {}", message);
    }
    public static void logException(Throwable t) {
        LOGGER.error("[Revelationary] ", t);
    }
    
    public Revelationary() {
        AdvancementCriteria.register();
        NeoForge.EVENT_BUS.register(this);
        RevelationaryNetworking.registerPacketReceivers();
    }
    
    @SubscribeEvent
    public void onInitialize() {
        logInfo("Starting Common Startup");

        RevelationaryNetworking.register();

        AdvancementCriteria.register();
        
        if (cursedChunkBuildingActive()) {
            logWarning("Sodium/Rubidium detected. Chunk rebuilding will be done in cursed mode.");
        }
        
        logInfo("Common startup completed!");
    }
    
    public static boolean cursedChunkBuildingActive() {
        LoadingModList modlist = LoadingModList.get();
        return modlist.getModFileById("sodium") != null || modlist.getModFileById("rubidium") != null;
    }
    
    @SubscribeEvent
    public void onAddReloadListener(@NotNull AddReloadListenerEvent event) {
        // TODO: is there a better location to trigger this?
        RevelationRegistry.addRevelationAwares();
        RevelationRegistry.deepTrim();
        
        event.addListener(RevelationDataLoader.INSTANCE);
    }
    
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        RevelationRegistry.addRevelationAwares();
    }
    
    @SubscribeEvent
    public void onCmdRegister(@NotNull RegisterCommandsEvent event) {
        Commands.register(event.getDispatcher());
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLogIn(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayerEntity) {
            RevelationaryNetworking.sendRevelations(serverPlayerEntity);
        }
    }
    
}
