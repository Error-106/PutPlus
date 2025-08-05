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

/**
 * PutPlus插件：用于提供放置相关功能的插件。
 *
 * @author Error-106
 * @date 2025/08/05
 */
public final class PutPlus extends JavaPlugin implements Listener {

    /**
     * 当插件被启用时调用。
     * 注册事件处理器，并在控制台输出插件启用信息。
     */
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

    /**
     * 获取Put命令的文本参数构造器
     *
     * @return 文本参数构造器实例，用于构建Put命令
     */
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

    /**
     * 使用命令上下文执行runPut方法。
     *
     * @param ctx 命令上下文
     * @return 执行结果
     */
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

    /**
     * 岗位枚举类：定义不同身体部位的位置枚举
     *
     * @author Error-106
     * @date 2025/08/05
     */
    public enum PositionEnum {
        HEAD,
        CHEST,
        LEGS,
        FEET;

        /**
         * 返回枚举常量的名称的小写形式。
         *
         * @return 枚举常量名称的小写表示
         */
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    /**
     * 方法枚举类：定义了两种状态，ON 和 OFF
     *
     * @author Error-106
     * @date 2025/08/05
     */
    public enum MethodEnum {
        ON,
        OFF;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    /**
     * PositionArgument 静态内部类：提供自定义参数类型转换的实现。
     *
     * @author Error-106
     * @date 2025/08/05
     */
    private static final class PositionArgument implements CustomArgumentType.Converted<@NotNull PositionEnum, @NotNull String> {

        /**
         * 获取原生类型的方法
         *
         * @return 字符串类型的原生ArgumentType
         */
        @Override
        public @NotNull ArgumentType<String> getNativeType() {
            return StringArgumentType.word();
        }

        /**
         * 获取建议列表的方法
         *
         * @param <S>     上下文类型参数
         * @param context 命令上下文
         * @param builder 建议构建器
         * @return 包含建议的CompletableFuture对象
         */
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

        /**
         * 将原生类型字符串转换为位置枚举
         *
         * @param nativeType 原生类型字符串
         * @return 对应的位置枚举值
         * @throws CommandSyntaxException 当转换失败时抛出异常
         */
        @Override
        public @NotNull PositionEnum convert(@NotNull String nativeType) throws CommandSyntaxException {
            try {
                return PositionEnum.valueOf(nativeType.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                throw new CommandException("错误的参数:" + nativeType);
            }
        }
    }

    /**
     * 内部静态类：用于实现自定义参数类型转换。
     * <p>
     * 该类实现了{@link CustomArgumentType.Converted}接口，针对{@link MethodEnum}枚举类型与String类型之间的转换。
     *
     * @author Error-106
     * @date 2025/08/05
     */
    private static final class MethodArgument implements CustomArgumentType.Converted<@NotNull MethodEnum, @NotNull String> {

        /**
         * 获取原生类型的方法
         *
         * @return 原生类型对象，此处为字符串类型的_ARGUMENT_TYPE
         */
        @Override
        public @NotNull ArgumentType<String> getNativeType() {
            return StringArgumentType.word();
        }

        /**
         * 获取建议列表的方法
         *
         * @param <S>     上下文类型参数
         * @param context 命令上下文
         * @param builder 建议构建器
         * @return 包含建议的CompletableFuture对象
         */
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

        /**
         * 将原生类型字符串转换为对应的方法枚举类型
         *
         * @param nativeType 原生类型字符串
         * @return 对应的方法枚举类型
         * @throws CommandSyntaxException 当转换失败时抛出异常
         */
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
