package com.zhanganzhi.mcdrcommand.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandUtil {
    public static <S> void removeChild(@NotNull CommandNode<S> commandNode,
                             @NotNull Map<String, CommandNode<S>> children,
                             @NotNull Map<String, LiteralCommandNode<S>> literals,
                             @NotNull Map<String, ArgumentCommandNode<S, ?>> arguments
    ) {
        if (commandNode instanceof RootCommandNode) {
            throw new UnsupportedOperationException("Cannot remove a RootCommandNode as a child from any other CommandNode");
        }
        final CommandNode<S> child = children.get(commandNode.getName());
        if (child == null) {
            throw new IllegalArgumentException("This CommandNode seems not to be registered to childrens.");
        } else {
            for (final CommandNode<S> grandChild : commandNode.getChildren()) {
                removeChild(grandChild, children, literals, arguments);
            }
            children.remove(commandNode.getName(), commandNode);
            if (commandNode instanceof LiteralCommandNode) {
                literals.remove(commandNode.getName(), commandNode);
            } else if (commandNode instanceof ArgumentCommandNode) {
                arguments.remove(commandNode.getName(), commandNode);
            }
        }
        children = children.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static <S> String unRegisterCommand(LiteralArgumentBuilder<S> command, CommandDispatcher<S> dispatcher) {
        try {
            CommandNode<S> commandNode = command.build();
            Field field = dispatcher.getClass().getDeclaredField("root");
            field.setAccessible(true);
            RootCommandNode<S> rootCommandNode = (RootCommandNode<S>) field.get(dispatcher);
            Class<?> clazz = rootCommandNode.getClass().getSuperclass();
            Field childrenField = clazz.getDeclaredField("children");
            childrenField.setAccessible(true);
            Field literalsField = clazz.getDeclaredField("literals");
            literalsField.setAccessible(true);
            Field argumentsField = clazz.getDeclaredField("arguments");
            argumentsField.setAccessible(true);
            Map<String, CommandNode<S>> children = (Map<String, CommandNode<S>>) childrenField.get(rootCommandNode);
            Map<String, LiteralCommandNode<S>> literals = (Map<String, LiteralCommandNode<S>>) literalsField.get(rootCommandNode);
            Map<String, ArgumentCommandNode<S, ?>> arguments = (Map<String, ArgumentCommandNode<S, ?>>) argumentsField.get(rootCommandNode);
            removeChild(commandNode, children, literals, arguments);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

}
