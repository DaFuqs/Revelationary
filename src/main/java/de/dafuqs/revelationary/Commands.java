package de.dafuqs.revelationary;

import com.mojang.brigadier.arguments.StringArgumentType;
import de.dafuqs.revelationary.api.advancements.AdvancementUtils;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;

public class Commands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher
                .register(CommandManager.literal("revelationary")
                        .then(CommandManager.literal("advancement")
                                .then(CommandManager.literal("revoke")
                                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                .then(CommandManager.argument("namespace", StringArgumentType.string())
                                                        .then(CommandManager.argument("path", StringArgumentType.string())
                                                                .executes(context -> {
                                                                    for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                                                                        AdvancementUtils.revokeAllAdvancements(player, StringArgumentType.getString(context, "namespace"), StringArgumentType.getString(context, "path"));
                                                                    }
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                                .then(CommandManager.literal("grant")
                                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                .then(CommandManager.argument("namespace", StringArgumentType.string())
                                                        .then(CommandManager.argument("path", StringArgumentType.string())
                                                                .executes(context -> {
                                                                    for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                                                                        AdvancementUtils.grandAllAdvancements(player, StringArgumentType.getString(context, "namespace"), StringArgumentType.getString(context, "path"));
                                                                    }
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }
}
