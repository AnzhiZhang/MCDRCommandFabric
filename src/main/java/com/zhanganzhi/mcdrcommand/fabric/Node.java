package com.zhanganzhi.mcdrcommand.fabric;

import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class Node {
    public static final Codec<Node> CODEC = Codec.recursive(
        "Node",
        self -> RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(node -> node.name),
            Codec.STRING.fieldOf("type").forGetter(node -> node.type),
            self.listOf().optionalFieldOf("children", List.of()).forGetter(node -> node.children)
        ).apply(instance, Node::new))
    );

    private static final DynamicCommandExceptionType NOT_LITERAL = new DynamicCommandExceptionType(arg -> new LiteralMessage("Invalid type, expected LITERAL but found '" + arg + "'"));

    private final String name;
    private final String type;
    private final List<Node> children;

    public Node(String name, String type, List<Node> children) {
        this.name = name;
        this.type = type;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public LiteralArgumentBuilder<CommandSourceStack> buildRoot() throws CommandSyntaxException {
        if (!"LITERAL".equals(this.type)) {
            throw NOT_LITERAL.create(this.type);
        }

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(this.name).executes(this::commonHandler);
        for (Node child : this.children) {
            root.then(child.build());
        }
        return root;
    }

    public ArgumentBuilder<CommandSourceStack, ?> build() {
        ArgumentBuilder<CommandSourceStack, ?> builder = switch (this.type) {
            case "LITERAL" -> Commands.literal(this.name);
            case "INTEGER" -> Commands.argument(this.name, IntegerArgumentType.integer());
            case "FLOAT" -> Commands.argument(this.name, DoubleArgumentType.doubleArg());
            case "QUOTABLE_TEXT" -> Commands.argument(this.name, StringArgumentType.string());
            case "GREEDY_TEXT" -> Commands.argument(this.name, StringArgumentType.greedyString());
            default -> Commands.argument(this.name, StringArgumentType.word());
        };
        for (Node child : this.children) {
            builder.then(child.build());
        }
        return builder.executes(this::commonHandler);
    }

    public int commonHandler(CommandContext<CommandSourceStack> context) {
        MCDRCommand.LOGGER.info(
            "<{}> {}",
            context.getSource().getTextName(),
            context.getInput()
        );
        return Command.SINGLE_SUCCESS;
    }

}
