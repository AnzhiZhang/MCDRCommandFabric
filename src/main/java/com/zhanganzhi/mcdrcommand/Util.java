package com.zhanganzhi.mcdrcommand;

import com.alibaba.fastjson2.JSONObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.Map;

public class Util {

    public static ArrayList<ArgumentBuilder<ServerCommandSource, ?>> argumentBuilders = new ArrayList<>();

    public static LiteralArgumentBuilder<ServerCommandSource> buildFromJson(JSONObject json) {
        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("commands");
        json.forEach((key, value) -> {
            root.then(buildCommandBuilder(key, value));
        });
        return root;
    }

    public static ArgumentBuilder<ServerCommandSource, ?> buildCommandBuilder(String key, Object value) {
        if (value instanceof String) {
            switch ((String) value) {
                case "INTEGER":
                    return CommandManager.argument(key.replace("Argument<", "").replace(">", ""), IntegerArgumentType.integer()).executes(context -> {
                        return 1;
                    });
                case "DOUBLE":
                    return CommandManager.argument(key.replace("Argument<", "").replace(">", ""), DoubleArgumentType.doubleArg()).executes(context -> {
                        return 1;
                    });
                case "GREEDY_STRING":
                    return CommandManager.argument(key.replace("Argument<", "").replace(">", ""), StringArgumentType.greedyString()).executes(context -> {
                        return 1;
                    });
                case "WORD":
                    return CommandManager.argument(key.replace("Argument<", "").replace(">", ""), StringArgumentType.word()).executes(context -> {
                        return 1;
                    });
                case "NOTHING":
                    return CommandManager.literal(key).executes(context -> {
                        return 1;
                    });
                default:
                    return null;
            }
        } else {
            LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal(key);
            if (value instanceof Map) {
                ((Map<String, Object>) value).forEach((key1, value1) -> {
                    builder.then(buildCommandBuilder(key1, value1));
                });
            }
            return builder;
        }
    }
}
