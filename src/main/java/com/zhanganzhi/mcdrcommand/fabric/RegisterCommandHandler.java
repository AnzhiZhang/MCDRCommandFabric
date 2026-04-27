package com.zhanganzhi.mcdrcommand.fabric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.serialization.JsonOps;
import com.zhanganzhi.mcdrcommand.fabric.mixin.CommandNodeAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.players.PlayerList;

public class RegisterCommandHandler implements Command<CommandSourceStack> {
    private final ArrayList<String> registeredCommands = new ArrayList<>();

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();

        // unregister commands
        RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();

        Map<String, CommandNode<?>> children = ((CommandNodeAccessor) root).getChildren();
        Map<String, LiteralCommandNode<?>> literals = ((CommandNodeAccessor) root).getLiterals();

        for (String literal : this.registeredCommands) {
            children.remove(literal);
            literals.remove(literal);
        }
        this.registeredCommands.clear();

        // register commands
        String data = StringArgumentType.getString(context, "data");
        JsonElement array = JsonParser.parseString(data).getAsJsonObject().get("data");

        Optional<List<Node>> result = Node.CODEC.listOf().parse(JsonOps.INSTANCE, array).result();
        if (result.isPresent()) {
            List<Node> nodes = result.get();
            for (Node node : nodes) {
                this.registeredCommands.add(node.getName());
                dispatcher.register(node.buildRoot());
            }
            context.getSource().sendSuccess(
                () -> Component.literal("Registered %s commands".formatted(nodes.size())),
                false
            );
        }
        // send command tree
        PlayerList playerList = server.getPlayerList();
        playerList.getPlayers().forEach(playerList::sendPlayerPermissionLevel);
        return Command.SINGLE_SUCCESS;
    }
}
