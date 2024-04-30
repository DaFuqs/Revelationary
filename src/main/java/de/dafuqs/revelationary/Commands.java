package de.dafuqs.revelationary;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import de.dafuqs.revelationary.api.advancements.AdvancementUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.stream.Collectors;

public class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var revelationaryNode = CommandManager
                .literal("revelationary")
                .requires(source -> source.hasPermissionLevel(4))
                .build();
        dispatcher.getRoot().addChild(revelationaryNode);

        var advancementNode = CommandManager.literal("advancement").build();
        revelationaryNode.addChild(advancementNode);

        var revokeNode = CommandManager.literal("revoke").build();
        advancementNode.addChild(revokeNode);
        executesWithTargetsNamespacePathArguments(revokeNode, Executors::revoke);

        var grantNode = CommandManager.literal("grant").build();
        advancementNode.addChild(grantNode);
        executesWithTargetsNamespacePathArguments(grantNode, Executors::grant);

        var syncNode = CommandManager.literal("sync").build();
        advancementNode.addChild(syncNode);
        var sourceSyncArgument = CommandManager
                .argument("source", EntityArgumentType.player())
                .build();
        var targetsSyncArgument = CommandManager
                .argument("targets", EntityArgumentType.players())
                .executes(context -> Executors.sync(context, false, false, false))
                .build();
        var namespaceSyncArgument = CommandManager
                .argument("namespace", StringArgumentType.string())
                .executes(context -> Executors.sync(context, true, false, false))
                .build();
        var pathSyncArgument = CommandManager
                .argument("path", StringArgumentType.string())
                .executes(context -> Executors.sync(context, true, true, false))
                .build();
        var deleteOldSyncArgument = CommandManager
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
        int execute(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets, String namespace, String path) throws CommandSyntaxException;
    }

    private static int retrieveArgumentsAndCallExecutor(CommandContext<ServerCommandSource> context, TargetsNamespacePathExecutor executor, boolean checkNamespace, boolean checkPath) throws CommandSyntaxException {
        return executor.execute(
                context,
                EntityArgumentType.getPlayers(context, "targets"),
                checkNamespace ? StringArgumentType.getString(context, "namespace") : "all",
                checkPath ? StringArgumentType.getString(context, "path") : "all");
    }

    private static CommandNode<ServerCommandSource> executesWithTargetsNamespacePathArguments(CommandNode<ServerCommandSource> parentNode, TargetsNamespacePathExecutor executor) {
        var targetsArgument = CommandManager
                .argument("targets", EntityArgumentType.players())
                .executes(context -> retrieveArgumentsAndCallExecutor(context, executor, false, false))
                .build();
        var namespaceArgument = CommandManager
                .argument("namespace", StringArgumentType.string())
                .executes(context -> retrieveArgumentsAndCallExecutor(context, executor, true, false))
                .build();
        var pathArgument = CommandManager
                .argument("path", StringArgumentType.string())
                .executes(context -> retrieveArgumentsAndCallExecutor(context, executor, true, true))
                .build();

        parentNode.addChild(targetsArgument);
        targetsArgument.addChild(namespaceArgument);
        namespaceArgument.addChild(pathArgument);

        return pathArgument;
    }

    // Utility function
    private static String joinPlayersList(Collection<ServerPlayerEntity> players) {
        return players.stream().map(player -> player.getDisplayName().getString()).collect(Collectors.joining(", "));
    }

    private static class Executors {
        private static int revoke(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets, String namespace, String path) {
            var count = targets.stream().mapToInt(player -> AdvancementUtils.forPlayer(player).withNamespace(namespace).withPath(path).revoke()).sum();
            context.getSource().sendFeedback(() -> Text.translatable("commands.revelationary.advancement.revoke", count, joinPlayersList(targets), namespace, path), false);
            return count;
        }

        private static int grant(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets, String namespace, String path) {
            var count = targets.stream().mapToInt(player -> AdvancementUtils.forPlayer(player).withNamespace(namespace).withPath(path).grant()).sum();
            context.getSource().sendFeedback(() -> Text.translatable("commands.revelationary.advancement.grant", count, joinPlayersList(targets), namespace, path), false);
            return count;
        }

        private static int sync(CommandContext<ServerCommandSource> context, boolean checkNamespace, boolean checkPath, boolean checkDeleteOld) throws CommandSyntaxException {
            var source = EntityArgumentType.getPlayer(context, "source");
            var targets = EntityArgumentType.getPlayers(context, "targets");
            var namespace = checkNamespace ? StringArgumentType.getString(context, "namespace") : "all";
            var path = checkPath ? StringArgumentType.getString(context, "path") : "all";
            var deleteOld = checkDeleteOld && BoolArgumentType.getBool(context, "deleteOld");

            var count = targets.stream().mapToInt(player -> AdvancementUtils.forPlayer(source).withNamespace(namespace).withPath(path).syncTo(player, deleteOld)).sum();
            context.getSource().sendFeedback(() -> Text.translatable("commands.revelationary.advancement.sync", count, source.getDisplayName(), joinPlayersList(targets), namespace, path), false);
            return count;
        }
    }
}