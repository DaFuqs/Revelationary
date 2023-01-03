package de.dafuqs.revelationary.api.advancements;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;

public class AdvancementUtils {
    public static int advCount = 0;

    public static void revokeAllAdvancements(ServerPlayerEntity player, String namespace , String path) {
        advCount = 0;
        if (player.getServer() == null) {
            return;
        }
        ServerAdvancementLoader sal = player.getServer().getAdvancementLoader();
        PlayerAdvancementTracker tracker = player.getAdvancementTracker();

        for (Advancement advancement : sal.getAdvancements()) {
            if (advancement.getId().getNamespace().equals(namespace) || namespace.equals("all")) {
                if (advancement.getId().getPath().startsWith(path) || path.equals("all")) {
                    advCount++;
                    for (String criterion : advancement.getCriteria().keySet()) {
                        tracker.revokeCriterion(advancement, criterion);
                    }
                }
            }
        }
    }
    public static void grantAllAdvancements(ServerPlayerEntity player, String namespace , String path) {
        advCount = 0;
        if (player.getServer() == null) {
            return;
        }
        ServerAdvancementLoader sal = player.getServer().getAdvancementLoader();
        PlayerAdvancementTracker tracker = player.getAdvancementTracker();

        for (Advancement advancement : sal.getAdvancements()) {
            if (advancement.getId().getNamespace().equals(namespace) || namespace.equals("all")) {
                if (advancement.getId().getPath().startsWith(path) || path.equals("all")) {
                    advCount++;
                    for (String criterion : advancement.getCriteria().keySet()) {
                        tracker.grantCriterion(advancement, criterion);
                    }
                }
            }
        }
    }
    public static void syncAdvancements(ServerPlayerEntity player1, ServerPlayerEntity player2, String namespace, String path, Boolean deleteOld) {
        advCount = 0;
        if (player1.getServer() == null || player2.getServer() == null) {
            return;
        }
        ServerAdvancementLoader sal = player1.getServer().getAdvancementLoader();
        PlayerAdvancementTracker tracker1 = player1.getAdvancementTracker();
        PlayerAdvancementTracker tracker2 = player2.getAdvancementTracker();

        if (deleteOld) {
            for (Advancement advancement : sal.getAdvancements()) {
                if (advancement.getId().getNamespace().equals(namespace) || namespace.equals("all")) {
                    if (advancement.getId().getPath().startsWith(path) || path.equals("all")) {
                        advCount++;
                        for (String criterion : advancement.getCriteria().keySet()) {
                            tracker2.revokeCriterion(advancement, criterion);
                        }
                    }
                }
            }
        }

        for (Advancement advancement : sal.getAdvancements()) {
            if (advancement.getId().getNamespace().equals(namespace) || namespace.equals("all")) {
                if (advancement.getId().getPath().startsWith(path) || path.equals("all")) {
                    advCount++;
                    for (String criterion : advancement.getCriteria().keySet()) {
                        if (tracker1.getProgress(advancement).isDone()) {
                            tracker2.grantCriterion(advancement, criterion);
                        }
                    }
                }
            }
        }

    }
}
