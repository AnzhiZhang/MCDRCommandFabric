package com.zhanganzhi.mcdrcommand.fabric;

import com.alibaba.fastjson2.JSONObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.zhanganzhi.mcdrcommand.Util;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

import java.util.Objects;

public class MCDRCommand implements ModInitializer {


    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("mcdr")
                    .then(CommandManager.literal("register").requires(
                            source -> source.getEntity() == null
                    ).then(
                            CommandManager.argument("tree", StringArgumentType.greedyString()).executes(
                                    context -> {
                                        context.getSource()
                                                .getMinecraftServer()
                                                .getCommandManager()
                                                .getDispatcher()
                                                .register(
                                                        CommandManager.literal("mcdr")
                                                                .then(
                                                                        Util.buildFromJson(
                                                                                JSONObject.parseObject(
                                                                                        StringArgumentType.getString(context, "tree")
                                                                                )
                                                                        )
                                                                )
                                                );
                                        context.getSource()
                                                .getMinecraftServer()
                                                .getPlayerManager()
                                                .getPlayerList()
                                                .forEach(serverPlayerEntity -> {
                                                    Objects.requireNonNull(serverPlayerEntity.getServer()).getPlayerManager().sendCommandTree(serverPlayerEntity);
                                                });
                                        return Command.SINGLE_SUCCESS;
                                    }
                            )
                    ))
            );
        }));
    }

}
