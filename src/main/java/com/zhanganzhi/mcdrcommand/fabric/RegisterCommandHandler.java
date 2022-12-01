package com.zhanganzhi.mcdrcommand.fabric;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;


public class RegisterCommandHandler implements Command<ServerCommandSource> {
    private final ArrayList<String> registeredCommands = new ArrayList<>();

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        MinecraftServer minecraftServer = context.getSource().getMinecraftServer();
        CommandDispatcher<ServerCommandSource> commandDispatcher = minecraftServer.getCommandManager().getDispatcher();

        // unregister commands
        try {
            // CommandDispatcher field
            Field rootCommandNodeField = commandDispatcher.getClass().getDeclaredField("root");
            rootCommandNodeField.setAccessible(true);

            // CommandNode fields
            Field commandNodeChildrenField = CommandNode.class.getDeclaredField("children");
            commandNodeChildrenField.setAccessible(true);
            Field commandNodeLiteralsField = CommandNode.class.getDeclaredField("literals");
            commandNodeLiteralsField.setAccessible(true);

            // children
            Map<String, CommandNode<ServerCommandSource>> children = (Map<String, CommandNode<ServerCommandSource>>) commandNodeChildrenField.get(rootCommandNodeField.get(commandDispatcher));
            Map<String, LiteralCommandNode<ServerCommandSource>> literals = (Map<String, LiteralCommandNode<ServerCommandSource>>) commandNodeChildrenField.get(rootCommandNodeField.get(commandDispatcher));
            for (String literal : registeredCommands) {
                children.remove(literal);
                literals.remove(literal);
            }
            registeredCommands.clear();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // register commands
        try {
            JSONObject jsonObject = JSON.parseObject(StringArgumentType.getString(context, "data"));
            for (JSONObject nodeJsonObject : jsonObject.getJSONArray("data").toArray(JSONObject.class)) {
                Node node = new Node(nodeJsonObject);
                registeredCommands.add(node.getName());
                commandDispatcher.register(node.getRootArgumentBuilder());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // send command tree
        minecraftServer.getPlayerManager().getPlayerList().forEach(
                serverPlayerEntity ->
                        Objects.requireNonNull(serverPlayerEntity.getServer())
                                .getPlayerManager()
                                .sendCommandTree(serverPlayerEntity)
        );

        return Command.SINGLE_SUCCESS;
    }
}
