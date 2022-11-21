package com.zhanganzhi.mcdrcommand.fabric;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.zhanganzhi.mcdrcommand.command.CommandUtil.unRegisterCommand;

public class MCDRCommand implements ModInitializer {
    LiteralArgumentBuilder<ServerCommandSource> someCommand = CommandManager.literal("wdnmd").executes(context1 -> {
                context1.getSource().sendFeedback(Text.of("WDNMD TOO"), false);
                return 0;
            }
    );


    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("test").executes(context -> {
                    context.getSource().sendFeedback(Text.of("Registering!"), false);
                    MinecraftServer server = context.getSource().getMinecraftServer();
                    server.getCommandManager().getDispatcher().register(
                            someCommand
                    );
                    server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                        serverPlayerEntity.sendMessage(Text.of("Sending Command to everyone!"), false);
                        server.getPlayerManager().sendCommandTree(serverPlayerEntity);
                    });
                    return 1;
                })
        ));
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("remove_command").executes(context -> {
                    context.getSource().sendFeedback(Text.of("Bye!"), false);
                    MinecraftServer server = context.getSource().getMinecraftServer();
                    String success = unRegisterCommand(someCommand, server.getCommandManager().getDispatcher());
                    if (success == null){
                        context.getSource().sendFeedback(Text.of("SUCCESS"),false);
                    }
                    else {
                        context.getSource().sendFeedback(Text.of(success),false);
                    }
                    server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                        serverPlayerEntity.sendMessage(Text.of("Sending Command to everyone!"), false);
                        server.getPlayerManager().sendCommandTree(serverPlayerEntity);
                    });
                    return 1;
                })
        ));
        //register();
    }

    private void register() {
        register(null, null);

    }

    private void register(MinecraftServer server, JSONObject tree) {
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

        // new root node
        LiteralArgumentBuilder<ServerCommandSource> rootNode = CommandManager.literal("mcdr");
        rootNode = rootNode.then(registerNode);
        CommandManager manager = server.getCommandManager();
        // add command tree to root node
        if (tree != null) {
            System.out.println(tree);
            tree.keySet().forEach(key -> {
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
            });
        }
        // register root node
        final LiteralArgumentBuilder<ServerCommandSource> finalRootNode = rootNode;
        //CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(finalRootNode));
        // update command tree
        server.getPlayerManager().getPlayerList().forEach(player -> server.getCommandManager().sendCommandTree(player));
        System.out.println("serer");
    }


}
