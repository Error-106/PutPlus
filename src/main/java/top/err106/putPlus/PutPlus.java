package top.err106.putPlus;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static top.err106.putPlus.PutCommand.runPut;

public final class PutPlus extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        final LifecycleEventManager<@NotNull Plugin> lifecycleManager = this.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, Command -> Command.registrar().register(getPutCommand().build()));
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage(Component.text("[")
                .append(Component.text("PutPlus").color(TextColor.color(0x55FF55)))
                .append(Component.text("] "))
                .append(Component.text("插件已启用").color(TextColor.color(0x55FF55)))
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getPutCommand() {
        return Commands.literal("put")
                .then(Commands.argument("method", new MethodArgument())
                        .then(Commands.argument("position", new PositionArgument())
                                .executes(PutPlus::runPutWithCtx)
                                .then(Commands.literal("force")
                                        .executes(PutPlus::runPutWithCtx)
                                )
                        )
                );
    }

    private static int runPutWithCtx(CommandContext<CommandSourceStack> ctx) {
        String method = ctx.getArgument("method", MethodEnum.class).toString();
        String position = ctx.getArgument("position", PositionEnum.class).toString();
        boolean force;
        try {
            force = ctx.getNodes().getLast().getNode().getName().equals("force");
        } catch (IndexOutOfBoundsException e) {
            force = false;
        }
        return runPut(ctx.getSource().getSender(), ctx.getSource().getExecutor(), method, position, force);
    }

    public enum PositionEnum {
        HEAD,
        CHEST,
        LEGS,
        FEET;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum MethodEnum {
        ON,
        OFF;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private static final class PositionArgument implements CustomArgumentType.Converted<PositionEnum, String> {

        @Override
        public @NotNull ArgumentType<String> getNativeType() {
            return StringArgumentType.word();
        }

        @Override
        public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
            for (PositionEnum position : PositionEnum.values()) {
                String name = position.toString();
                if (name.startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(position.toString());
                }
            }
            return builder.buildFuture();
        }

        @Override
        public @NotNull PositionEnum convert(@NotNull String nativeType) throws CommandSyntaxException {
            try {
                return PositionEnum.valueOf(nativeType.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                throw new CommandException("错误的参数:" + nativeType);
            }
        }
    }

    private static final class MethodArgument implements CustomArgumentType.Converted<MethodEnum, String> {

        @Override
        public @NotNull ArgumentType<String> getNativeType() {
            return StringArgumentType.word();
        }

        @Override
        public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
            for (MethodEnum method : MethodEnum.values()) {
                String name = method.toString();
                if (name.startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(method.toString());
                }
            }
            return builder.buildFuture();
        }

        @Override
        public @NotNull MethodEnum convert(@NotNull String nativeType) throws CommandSyntaxException {
            try {
                return MethodEnum.valueOf(nativeType.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                throw new CommandException("错误的参数:" + nativeType);
            }
        }
    }
}
