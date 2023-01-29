package de.dafuqs.revelationary.api.advancements;

import de.dafuqs.revelationary.advancement_criteria.AdvancementGottenCriterion;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiConsumer;

public class AdvancementUtils {
    protected String namespace = "all";
    protected String path = "all";
    protected final ServerPlayerEntity player;
    protected final ServerAdvancementLoader advancementLoader;
    protected final PlayerAdvancementTracker advancementTracker;

    protected AdvancementUtils(ServerPlayerEntity player) {
        this.player = player;
        advancementLoader = player.getServer().getAdvancementLoader();
        advancementTracker = player.getAdvancementTracker();
    }

    public static AdvancementUtils forPlayer(ServerPlayerEntity player) {
        return new AdvancementUtils(player);
    }

    public AdvancementUtils withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public AdvancementUtils withPath(String path) {
        this.path = path;
        return this;
    }

    public int grant() {
        return act(advancementTracker::grantCriterion);
    }

    public int revoke() {
        return act(advancementTracker::revokeCriterion);
    }

    public int syncTo(ServerPlayerEntity targetPlayer, boolean deleteOld) {
        var count = 0;
        var targetAdvancementTracker = targetPlayer.getAdvancementTracker();

        if (deleteOld) {
            count += act(targetAdvancementTracker::revokeCriterion);
        }

        count += act((advancement, criterion) -> {
            if (advancementTracker.getProgress(advancement).isDone()) {
                targetAdvancementTracker.grantCriterion(advancement, criterion);
            }
        });

        return count;
    }

    /**
     * Reprocesses all AdvancementGottenCriteria and fixes all instances
     * where the player has an advancement, but a criterion that is set to get triggered
     * by it is not granted. (like after you changed your mods advancement criteria in an update)
     * Can only use used on the logical server
     */
    public void reprocessUnlocks() {
        for (var advancement : advancementLoader.getAdvancements()) {
            if (advancement.getId().getNamespace().equals(namespace) && !advancementTracker.getProgress(advancement).isDone()) {
                for (var criterionEntry : advancement.getCriteria().entrySet()) {
                    // 1: instanceof checks for null automatically
                    // 2: AdvancementGottenCriterion.Conditions will always have the appropriate ID, no need to check for that
                    if (criterionEntry.getValue().getConditions() instanceof AdvancementGottenCriterion.Conditions gottenConditions) {
                        var gottenAdvancement = advancementLoader.get(gottenConditions.getAdvancementIdentifier());
                        if (gottenAdvancement != null && advancementTracker.getProgress(gottenAdvancement).isDone()) {
                            advancementTracker.grantCriterion(advancement, criterionEntry.getKey());
                        }
                    }
                }
            }
        }
    }

    protected int act(BiConsumer<Advancement, String> action) {
        var count = 0;

        for (var advancement : advancementLoader.getAdvancements()) {
            if (advancement.getId().getNamespace().equals(namespace) || namespace.equals("all")) {
                if (advancement.getId().getPath().startsWith(path) || path.equals("all")) {
                    count++;
                    for (var criterion : advancement.getCriteria().keySet()) {
                        action.accept(advancement, criterion);
                    }
                }
            }
        }

        return count;
    }

    @Deprecated
    public static int revokeAllAdvancements(ServerPlayerEntity player, String namespace , String path) {
        return forPlayer(player).withNamespace(namespace).withPath(path).revoke();
    }

    @Deprecated
    public static int grantAllAdvancements(ServerPlayerEntity player, String namespace , String path) {
        return forPlayer(player).withNamespace(namespace).withPath(path).grant();
    }

    @Deprecated
    public static int syncAdvancements(ServerPlayerEntity sourcePlayer, ServerPlayerEntity targetPlayer, String namespace, String path, Boolean deleteOld) {
        return forPlayer(sourcePlayer).withNamespace(namespace).withPath(path).syncTo(targetPlayer, deleteOld);
    }

    @Deprecated
    public static void reprocessAdvancementUnlocks(ServerPlayerEntity player, String namespace) {
        forPlayer(player).withNamespace(namespace).reprocessUnlocks();
    }
}
