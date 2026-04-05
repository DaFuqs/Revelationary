package de.dafuqs.revelationary;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import de.dafuqs.revelationary.api.advancements.AdvancementUtils;
import java.util.Collection;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class RevelationaryCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        var revelationaryNode = Commands
                .literal("revelationary")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .build();
        dispatcher.getRoot().addChild(revelationaryNode);

        var advancementNode = Commands.literal("advancement").build();
        revelationaryNode.addChild(advancementNode);

        var revokeNode = Commands.literal("revoke").build();
        advancementNode.addChild(revokeNode);
        executesWithTargetsNamespacePathArguments(revokeNode, Executors::revoke);

        var grantNode = Commands.literal("grant").build();
        advancementNode.addChild(grantNode);
        executesWithTargetsNamespacePathArguments(grantNode, Executors::grant);

        var syncNode = Commands.literal("sync").build();
        advancementNode.addChild(syncNode);
        var sourceSyncArgument = Commands
                .argument("source", EntityArgument.player())
                .build();
        var targetsSyncArgument = Commands
                .argument("targets", EntityArgument.players())
                .executes(context -> Executors.sync(context, false, false, false))
                .build();
        var namespaceSyncArgument = Commands
                .argument("namespace", StringArgumentType.string())
                .executes(context -> Executors.sync(context, true, false, false))
                .build();
        var pathSyncArgument = Commands
                .argument("path", StringArgumentType.string())
                .executes(context -> Executors.sync(context, true, true, false))
                .build();
        var deleteOldSyncArgument = Commands
                .argument("deleteOld", BoolArgumentType.bool())
                .executes(context -> Executors.sync(context, true, true, true))
                .build();

        syncNode.addChild(sourceSyncArgument);
        sourceSyncArgument.addChild(targetsSyncArgument);
        targetsSyncArgument.addChild(namespaceSyncArgument);
        namespaceSyncArgument.addChild(pathSyncArgument);
        pathSyncArgument.addChild(deleteOldSyncArgument);
    }

    @FunctionalInterface
    private interface TargetsNamespacePathExecutor {
        int execute(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, String namespace, String path) throws CommandSyntaxException;
    }

    private static int retrieveArgumentsAndCallExecutor(CommandContext<CommandSourceStack> context, TargetsNamespacePathExecutor executor, boolean checkNamespace, boolean checkPath) throws CommandSyntaxException {
        return executor.execute(
                context,
                EntityArgument.getPlayers(context, "targets"),
                checkNamespace ? StringArgumentType.getString(context, "namespace") : "all",
                checkPath ? StringArgumentType.getString(context, "path") : "all");
    }

    private static CommandNode<CommandSourceStack> executesWithTargetsNamespacePathArguments(CommandNode<CommandSourceStack> parentNode, TargetsNamespacePathExecutor executor) {
        var targetsArgument = Commands
                .argument("targets", EntityArgument.players())
                .executes(context -> retrieveArgumentsAndCallExecutor(context, executor, false, false))
                .build();
        var namespaceArgument = Commands
                .argument("namespace", StringArgumentType.string())
                .executes(context -> retrieveArgumentsAndCallExecutor(context, executor, true, false))
                .build();
        var pathArgument = Commands
                .argument("path", StringArgumentType.string())
                .executes(context -> retrieveArgumentsAndCallExecutor(context, executor, true, true))
                .build();

        parentNode.addChild(targetsArgument);
        targetsArgument.addChild(namespaceArgument);
        namespaceArgument.addChild(pathArgument);

        return pathArgument;
    }

    // Utility function
    private static String joinPlayersList(Collection<ServerPlayer> players) {
        return players.stream().map(player -> player.getDisplayName().getString()).collect(Collectors.joining(", "));
    }

    private static class Executors {
        private static int revoke(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, String namespace, String path) {
            var count = targets.stream().mapToInt(player -> AdvancementUtils.forPlayer(player).withNamespace(namespace).withPath(path).revoke()).sum();
            context.getSource().sendSuccess(() -> Component.translatable("commands.revelationary.advancement.revoke", count, joinPlayersList(targets), namespace, path), false);
            return count;
        }

        private static int grant(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, String namespace, String path) {
            var count = targets.stream().mapToInt(player -> AdvancementUtils.forPlayer(player).withNamespace(namespace).withPath(path).grant()).sum();
            context.getSource().sendSuccess(() -> Component.translatable("commands.revelationary.advancement.grant", count, joinPlayersList(targets), namespace, path), false);
            return count;
        }

        private static int sync(CommandContext<CommandSourceStack> context, boolean checkNamespace, boolean checkPath, boolean checkDeleteOld) throws CommandSyntaxException {
            var source = EntityArgument.getPlayer(context, "source");
            var targets = EntityArgument.getPlayers(context, "targets");
            var namespace = checkNamespace ? StringArgumentType.getString(context, "namespace") : "all";
            var path = checkPath ? StringArgumentType.getString(context, "path") : "all";
            var deleteOld = checkDeleteOld && BoolArgumentType.getBool(context, "deleteOld");

            var count = targets.stream().mapToInt(player -> AdvancementUtils.forPlayer(source).withNamespace(namespace).withPath(path).syncTo(player, deleteOld)).sum();
            context.getSource().sendSuccess(() -> Component.translatable("commands.revelationary.advancement.sync", count, source.getDisplayName(), joinPlayersList(targets), namespace, path), false);
            return count;
        }
    }
}