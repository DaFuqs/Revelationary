package de.dafuqs.revelationary;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.dafuqs.revelationary.api.advancements.AdvancementUtils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

import static de.dafuqs.revelationary.Revelationary.logError;

public class Commands {

    //register the main commands
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("revelationary")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("advancement")
                        .then(CommandManager.literal("revoke")
                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                        .executes(context -> {
                                            executeAdv("revoke", context, null, null);
                                            return 1;
                                        })
                                        .then(CommandManager.argument("namespace", StringArgumentType.string())
                                                .executes(context -> {
                                                    executeAdv("revoke", context, StringArgumentType.getString(context, "namespace"), null);
                                                    return 1;
                                                })
                                                .then(CommandManager.argument("path", StringArgumentType.string())
                                                        .executes(context -> {
                                                            executeAdv("revoke", context, StringArgumentType.getString(context, "namespace"), StringArgumentType.getString(context, "path"));
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(CommandManager.literal("grant")
                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                        .executes(context -> {
                                            executeAdv("grant", context, null, null);
                                            return 1;
                                        })
                                        .then(CommandManager.argument("namespace", StringArgumentType.string())
                                                .executes(context -> {
                                                    executeAdv("revoke", context, StringArgumentType.getString(context, "namespace"), null);
                                                    return 1;
                                                })
                                                .then(CommandManager.argument("path", StringArgumentType.string())
                                                        .executes(context -> {
                                                            executeAdv("grant", context, StringArgumentType.getString(context, "namespace"), StringArgumentType.getString(context, "path"));
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(CommandManager.literal("sync")
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                .executes(context -> {
                                                    executeAdv("sync", context, null, null);
                                                    return 1;
                                                })
                                                .then(CommandManager.argument("namespace", StringArgumentType.string())
                                                        .executes(context -> {
                                                            executeAdv("sync", context, StringArgumentType.getString(context, "namespace"), null);
                                                            return 1;
                                                        })
                                                        .then(CommandManager.argument("path", StringArgumentType.string())
                                                                .executes(context -> {
                                                                    executeAdv("sync", context, StringArgumentType.getString(context, "namespace"), StringArgumentType.getString(context, "path"));
                                                                    return 1;
                                                                })
                                                                .then(CommandManager.argument("deleteOld", BoolArgumentType.bool())
                                                                        .executes(context -> {
                                                                            executeAdv("sync", context, StringArgumentType.getString(context, "namespace"), StringArgumentType.getString(context, "path"));
                                                                            return 1;
                                                                        })
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        ));
    }

    //probably a better way to do this? I think minecraft does it in another way so maybe changing it to that could be neater? But the switch statement sounded better at the start and now Im not gonna change it
    private static void executeAdv(String command, CommandContext<ServerCommandSource> context, String namespace, String path) {
        Map<String, String> args = getCommandArgMap(context, namespace, path);
        String utilNamespace = args.get("namespace").replace("*", "all");
        String utilPath = args.get("path").replace("*", "all");
        try {
            switch (command) {
                case "revoke" -> {
                    int advCount = 0;
                    for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                        advCount += AdvancementUtils.revokeAllAdvancements(player, utilNamespace, utilPath);
                    }
                    context.getSource().getPlayer().sendMessage(Text.translatable("commands.revelationary.advancement.revoke", advCount, args.get("targets"), args.get("namespace"), args.get("path")), false);
                }
                case "grant" -> {
                    int advCount = 0;
                    for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                        advCount += AdvancementUtils.grantAllAdvancements(player, utilNamespace, utilPath);
                    }
                    context.getSource().getPlayer().sendMessage(Text.translatable("commands.revelationary.advancement.grant", advCount, args.get("targets"), args.get("namespace"), args.get("path")), false);
                }
                case "sync" -> {
                    int advCount = 0;
                    for (ServerPlayerEntity targetPlayer : EntityArgumentType.getPlayers(context, "targets")) {
                        try {
                            advCount += AdvancementUtils.syncAdvancements(EntityArgumentType.getPlayer(context, "target"), targetPlayer, utilNamespace, utilPath, BoolArgumentType.getBool(context, "deleteOld"));
                        } catch (Exception e) {
                            advCount += AdvancementUtils.syncAdvancements(EntityArgumentType.getPlayer(context, "target"), targetPlayer, utilNamespace, utilPath, false);
                        }
                    }
                    context.getSource().getPlayer().sendMessage(Text.translatable("commands.revelationary.advancement.sync", advCount, EntityArgumentType.getPlayer(context, "target").getDisplayName(), args.get("targets"), args.get("namespace"), args.get("path")), false);
                }
            }
        } catch (Exception e) {
            logError("Error while executing command: " + e);
        }
    }

    private static Map<String, String> getCommandArgMap(CommandContext<ServerCommandSource> context, String namespace, String path) {
        //the if statements could probably be compacted
        try {
            if (namespace != null) {
                namespace = StringArgumentType.getString(context, "namespace");
                if (namespace.equals("all")) {
                    namespace = "*";
                }
            } else {
                namespace = "*";
            }
            if (path != null) {
                path = StringArgumentType.getString(context, "path");
                if (path.equals("all")) {
                    path = "*";
                }
            } else {
                path = "*";
            }
            String targets = EntityArgumentType.getPlayers(context, "targets").stream().map(player -> player.getDisplayName().getString()).reduce((a, b) -> a + ", " + b).orElse("null");
            return Map.of("targets", targets, "namespace", namespace, "path", path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}