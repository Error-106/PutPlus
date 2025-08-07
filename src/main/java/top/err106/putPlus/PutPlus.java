package top.err106.putPlus;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
     * Put命令的文本参数构造器
     *
     * @return 文本参数构造器实例，用于构建Put命令
     */
    private static LiteralArgumentBuilder<CommandSourceStack> getPutCommand() {
        return Commands.literal("put")
                .requires(sender -> sender.getSender().hasPermission("permission.putplus"))
                .then(Commands.argument("method", new MethodArgument())
                        .suggests(PutPlus::methodArgumentSuggests)
                        .then(Commands.argument("position", new PositionArgument())
                                .suggests(PutPlus::positionArgumentSuggests)
                                .executes(PutPlus::runPutWithCtx)
                                .then(Commands.literal("force")
                                        .requires(sender -> sender.getSender().hasPermission("permission.putplus.force"))
                                        .executes(PutPlus::runPutWithCtx)
                                )
                        )
                );
    }

    /**
     * 使用命令上下文检查权限并执行runPut方法。使用命令上下文检查权限并执行runPut方法。
     *
     * @param ctx 命令源栈的上下文
     * @return 执行结果
     */
    private static int runPutWithCtx(CommandContext<CommandSourceStack> ctx) {
        String method = ctx.getArgument("method", MethodEnum.class).toString();
        String position = ctx.getArgument("position", PositionEnum.class).toString();
        if (!ctx.getSource().getSender().hasPermission("permission.putplus." + method + "." + position)) {ctx.getSource().getSender().sendRichMessage("<red>你没有权限");return 0;}
        boolean force;
        try {
            force = ctx.getNodes().getLast().getNode().getName().equals("force");
        } catch (IndexOutOfBoundsException e) {
            force = false;
        }
        return runPut(ctx.getSource().getSender(), ctx.getSource().getExecutor(), method, position, force);
    }

    /**
     * 异步生成methodArgument建议。
     *
     * @param ctx     命令上下文
     * @param builder 建议构建器
     * @return 包含建议的CompletableFuture对象
     */
    private static CompletableFuture<Suggestions> methodArgumentSuggests(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder){
        return CompletableFuture.supplyAsync(()->{
            for (MethodEnum method : MethodEnum.values()) {
                String name = method.toString();
                if (name.startsWith(builder.getRemainingLowerCase())&&ctx.getSource().getSender().hasPermission("permission.putplus." + method)){
                    builder.suggest(method.toString());
                }
            }
            return builder.build();
        });
    }

    /**
     * 异步生成positionArgument建议。
     *
     * @param ctx     命令上下文
     * @param builder 建议构建器
     * @return 包含建议的CompletableFuture对象
     */
    private static CompletableFuture<Suggestions> positionArgumentSuggests(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder){
        return CompletableFuture.supplyAsync(()->{
            for (PositionEnum position : PositionEnum.values()) {
                String name = position.toString();
                if (name.startsWith(builder.getRemainingLowerCase())&&ctx.getSource().getSender().hasPermission("permission.putplus." + ctx.getArgument("method",MethodEnum.class).toString() + "." + position)){
                    builder.suggest(position.toString());
                }
            }
            return builder.build();
        });
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
         * 将原生类型字符串转换为位置枚举
         *
         * @param nativeType 原生类型字符串
         * @return 对应的位置枚举值
         */
        @Override
        public @NotNull PositionEnum convert(@NotNull String nativeType) {
            try {
                return PositionEnum.valueOf(nativeType.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                throw new CommandException("错误的参数:" + nativeType);
            }
        }
    }

    /**
     * MethodArgument 静态内部类：提供自定义参数类型转换的实现。
     *
     * @author Error-106
     * @date 2025/08/05
     */
    private static final class MethodArgument implements CustomArgumentType.Converted<@NotNull MethodEnum, @NotNull String> {

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
         * 将原生类型字符串转换为位置枚举
         *
         * @param nativeType 原生类型字符串
         * @return 对应的位置枚举值
         */
        @Override
        public @NotNull MethodEnum convert(@NotNull String nativeType) {
            try {
                return MethodEnum.valueOf(nativeType.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                throw new CommandException("错误的参数:" + nativeType);
            }
        }
    }
}
