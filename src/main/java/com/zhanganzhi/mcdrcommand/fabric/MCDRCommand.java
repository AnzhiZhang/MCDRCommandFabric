package com.zhanganzhi.mcdrcommand.fabric;

import net.minecraft.server.command.CommandManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class MCDRCommand implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) -> dispatcher.register(
                        CommandManager.literal("mcdr")
                                .requires(source -> source.getEntity() == null)
                                .then(
                                        CommandManager.literal("register")
                                                .then(
                                                        CommandManager.argument("data", StringArgumentType.greedyString())
                                                                .executes(new RegisterCommandHandler())
                                                )
                                )
                )
        );
    }
}
