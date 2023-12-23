package com.zhanganzhi.mcdrcommand.fabric;

import java.util.ArrayList;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import com.alibaba.fastjson2.JSONObject;

public class Node {
    private final String name;
    private final String type;
    private final ArrayList<Node> children = new ArrayList<>();

    public Node(JSONObject jsonObject) {
        name = jsonObject.getString("name");
        type = jsonObject.getString("type");
        for (JSONObject child : jsonObject.getJSONArray("children").toArray(JSONObject.class)) {
            children.add(new Node(child));
        }
    }

    public String getName() {
        return name;
    }

    public LiteralArgumentBuilder<ServerCommandSource> getRootArgumentBuilder() {
        return (LiteralArgumentBuilder<ServerCommandSource>) getArgumentBuilder().executes(new CommonCommandHandler());
    }

    public ArgumentBuilder<ServerCommandSource, ?> getArgumentBuilder() {
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder;
        switch (type) {
            case "LITERAL":
                argumentBuilder = CommandManager.literal(name);
                break;
            case "INTEGER":
                argumentBuilder = CommandManager.argument(name, IntegerArgumentType.integer());
                break;
            case "FLOAT":
                argumentBuilder = CommandManager.argument(name, DoubleArgumentType.doubleArg());
                break;
            case "QUOTABLE_TEXT":
                argumentBuilder = CommandManager.argument(name, StringArgumentType.string());
                break;
            case "GREEDY_TEXT":
                argumentBuilder = CommandManager.argument(name, StringArgumentType.greedyString());
                break;
            default:
                // NUMBER, TEXT, BOOLEAN, ENUMERATION, etc...
                argumentBuilder = CommandManager.argument(name, StringArgumentType.word());
                break;
        }
        argumentBuilder.executes(new CommonCommandHandler());
        for (Node child : children) {
            argumentBuilder.then(child.getArgumentBuilder());
        }
        return argumentBuilder;
    }
}
