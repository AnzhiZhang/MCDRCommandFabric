package com.zhanganzhi.mcdrcommand.fabric;

import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCDRCommand implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("MCDRCommand");


    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("mcdr")
                    .requires(source -> source.getEntity() == null)
                    .then(
                        Commands.literal("register")
                            .then(
                                Commands.argument("data", StringArgumentType.greedyString())
                                    .executes(new RegisterCommandHandler())
                            )
                    )
            )
        );
    }
}
