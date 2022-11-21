package com.zhanganzhi.mcdrcommand.fabric;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.world.ServerWorld;

public class MCDRCommand implements ModInitializer {
    LiteralArgumentBuilder<ServerCommandSource> registerNode = CommandManager.literal("register").requires(
            source -> source.getEntity() == null
    ).then(
            CommandManager.argument("tree", StringArgumentType.greedyString()).executes(
                    context -> {
                        register(
                                context.getSource().getMinecraftServer(),
                                JSON.parseObject(StringArgumentType.getString(context, "tree"))
                        );
                        return Command.SINGLE_SUCCESS;
                    }
            )
    );

    @Override
    public void onInitialize() {
        register();
    }

    private void register() {
        register(null, null);
    }

    private void register(MinecraftServer server, JSONObject tree) {
        // new root node
        LiteralArgumentBuilder<ServerCommandSource> rootNode = CommandManager.literal("mcdr");
        rootNode = rootNode.then(registerNode);

        // add command tree to root node
        if (tree != null) {
            System.out.println(tree);
            for (String key : tree.keySet()) {
                System.out.println(key.substring(2));
                CommandRegistrationCallback.EVENT.register(
                        (dispatcher, dedicated) -> dispatcher.register(
                                CommandManager.literal(key.substring(2)).executes(
                                        context -> {
                                            System.out.println(111);
                                            return 1;
                                        }
                                )
                        )
                );
//                rootNode.then(CommandManager.literal(key.substring(2)));
            }
        }

        // register root node
        final LiteralArgumentBuilder<ServerCommandSource> finalRootNode = rootNode;
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(finalRootNode));
        // update command tree
        if (server != null) {
            server.getPlayerManager().getPlayerList().forEach(player -> server.getCommandManager().sendCommandTree(player));
            System.out.println("serer");
        }
    }
}
