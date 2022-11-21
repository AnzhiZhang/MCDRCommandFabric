package com.zhanganzhi.mcdrcommand.fabric;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zhanganzhi.mcdrcommand.Util;
import com.zhanganzhi.mcdrcommand.command.CommandUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

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
                                        CommandDispatcher<ServerCommandSource> commandDispatcher = context.getSource()
                                                .getMinecraftServer()
                                                .getCommandManager()
                                                .getDispatcher();
                                        Util.argumentBuilders.forEach(serverCommandSourceArgumentBuilder -> {
                                            //unregister all of them
                                            CommandUtil.unRegisterCommand((LiteralArgumentBuilder<ServerCommandSource>) serverCommandSourceArgumentBuilder, commandDispatcher);
                                        });
                                        Util.argumentBuilders.clear();
                                        JSONObject jsonObject = (JSONObject) JSON.parse(StringArgumentType.getString(context,"tree"));
                                        jsonObject.forEach((s, o) -> {
                                            //and register them back
                                            ArgumentBuilder<ServerCommandSource,?> argumentBuilder = Util.buildCommandBuilder(s,o);
                                            Util.argumentBuilders.add(argumentBuilder);
                                            commandDispatcher.register((LiteralArgumentBuilder<ServerCommandSource>) argumentBuilder);
                                        });
                                        context.getSource()
                                                .getMinecraftServer()
                                                .getPlayerManager()
                                                .getPlayerList()
                                                .forEach(serverPlayerEntity -> Objects.requireNonNull(serverPlayerEntity.getServer()).getPlayerManager().sendCommandTree(serverPlayerEntity));
                                        return Command.SINGLE_SUCCESS;
                                    }
                            )
                    ))
            );
        }));
    }

}
