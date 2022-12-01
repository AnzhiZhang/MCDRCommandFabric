package com.zhanganzhi.mcdrcommand.fabric;

import org.apache.logging.log4j.LogManager;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

public class CommonCommandHandler implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        LogManager.getLogger().info(
                "<{}> {}",
                context.getSource().getName(),
                context.getInput().substring(1)
        );
        return Command.SINGLE_SUCCESS;
    }
}
