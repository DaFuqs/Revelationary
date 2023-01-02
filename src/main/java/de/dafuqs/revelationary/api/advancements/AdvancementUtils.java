package de.dafuqs.revelationary.api.advancements;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;

public class AdvancementUtils {
    public static void revokeAllAdvancements(ServerPlayerEntity player, String namespace , String path) {
        if (player.getServer() == null) {
            return;
        }
        ServerAdvancementLoader sal = player.getServer().getAdvancementLoader();
        PlayerAdvancementTracker tracker = player.getAdvancementTracker();

        for (Advancement advancement : sal.getAdvancements()) {
            if (advancement.getId().getNamespace().equals(namespace) && advancement.getId().getPath().startsWith(path)) {
                for (String criterion : advancement.getCriteria().keySet()) {
                    tracker.revokeCriterion(advancement, criterion);
                }
            }
        }
    }
    public static void grandAllAdvancements(ServerPlayerEntity player, String namespace , String path) {
        if (player.getServer() == null) {
            return;
        }
        ServerAdvancementLoader sal = player.getServer().getAdvancementLoader();
        PlayerAdvancementTracker tracker = player.getAdvancementTracker();

        for (Advancement advancement : sal.getAdvancements()) {
            if (advancement.getId().getNamespace().equals(namespace) && advancement.getId().getPath().startsWith(path)) {
                for (String criterion : advancement.getCriteria().keySet()) {
                    tracker.grantCriterion(advancement, criterion);
                }
            }
        }
    }
}
