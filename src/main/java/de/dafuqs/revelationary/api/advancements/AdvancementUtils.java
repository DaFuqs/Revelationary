package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.advancement_criteria.*;
import net.minecraft.advancement.*;
import net.minecraft.advancement.criterion.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.util.*;

import java.util.*;

public class AdvancementUtils {

    public static int revokeAllAdvancements(ServerPlayerEntity player, String namespace , String path) {
        int advCount = 0;
        if (player.getServer() == null) {
            return 0;
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
        return advCount;
    }

    public static int grantAllAdvancements(ServerPlayerEntity player, String namespace , String path) {
        int advCount = 0;
        if (player.getServer() == null) {
            return 0;
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
        return advCount;
    }

    public static int syncAdvancements(ServerPlayerEntity player1, ServerPlayerEntity player2, String namespace, String path, Boolean deleteOld) {
        int advCount = 0;
        if (player1.getServer() == null || player2.getServer() == null) {
            return 0;
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
        return advCount;
    }

    /**
     * Reprocesses all AdvancementGottenCriteria and fixes all instances
     * where the player has an advancement, but a criterion that is set to get triggered
     * by it is not granted. (like after you changed your mods advancement criteria in an update)
     * Can only use used on the logical server
     *
     * @param serverPlayerEntity The player to reprocess unlocks
     * @param namespace the namespace to reprocess. Usually will match your mod id
     */
    public static void reprocessAdvancementUnlocks(ServerPlayerEntity serverPlayerEntity, String namespace) {
        if (serverPlayerEntity.getServer() == null) {
            return;
        }

        PlayerAdvancementTracker tracker = serverPlayerEntity.getAdvancementTracker();
        ServerAdvancementLoader loader = serverPlayerEntity.getServer().getAdvancementLoader();
        for (Advancement advancement : loader.getAdvancements()) {
            if (advancement.getId().getNamespace().equals(namespace)) {
                AdvancementProgress hasAdvancement = tracker.getProgress(advancement);
                if (!hasAdvancement.isDone()) {
                    for (Map.Entry<String, AdvancementCriterion> criterionEntry : advancement.getCriteria().entrySet()) {
                        CriterionConditions conditions = criterionEntry.getValue().getConditions();
                        if (conditions != null && conditions.getId().equals(AdvancementGottenCriterion.ID) && conditions instanceof AdvancementGottenCriterion.Conditions hasAdvancementConditions) {
                            Identifier advancementIdentifier = hasAdvancementConditions.getAdvancementIdentifier();
                            Advancement advancementCriterionAdvancement = loader.get(advancementIdentifier);
                            if (advancementCriterionAdvancement != null) {
                                AdvancementProgress hasAdvancementCriterionAdvancement = tracker.getProgress(advancementCriterionAdvancement);
                                if (hasAdvancementCriterionAdvancement.isDone()) {
                                    tracker.grantCriterion(advancement, criterionEntry.getKey());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
