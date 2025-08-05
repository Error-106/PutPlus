package top.err106.putPlus;

import com.mojang.brigadier.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * PutCommand 类：用于处理放置命令逻辑
 *
 * @author Error-106
 * @date 2025/08/05
 */
public class PutCommand {

    /**
     * 执行放置武器操作的方法。
     *
     * @param sender   命令发送者
     * @param executor 命令执行者
     * @param method   操作方法，"on" 或 "off"，表示穿上/替换或取下装备
     * @param position 目标装备位置
     * @param force    是否强制执行操作
     * @return 命令执行结果，返回 {@link Command#SINGLE_SUCCESS} 表示执行成功
     */
    public static int runPut(CommandSender sender, Entity executor, String method, String position, boolean force) {
        if (checkIsPlayer(sender, executor)) return Command.SINGLE_SUCCESS;
        PlayerInventory executorInventory = ((Player) executor).getInventory();
        ItemStack executorArms = getPlayerArms(executorInventory, position);
        if (!force) if (checkInvEnchant(executorArms, sender)) return Command.SINGLE_SUCCESS;
        if (method.equals("on")) {
            PlayerInventory senderInventory = ((Player) sender).getInventory();
            ItemStack senderMainHandItem = senderInventory.getItemInMainHand();
            senderInventory.setItemInMainHand(ItemStack.empty());
            setPlayerArms(executorInventory,senderMainHandItem,position);
            senderInventory.setItemInMainHand(executorArms);
            //返回成功信息
        }else if (method.equals("off")){
            if (executorArms!=null) {
                setPlayerArms(executorInventory,ItemStack.empty(),position);
                ((Player) sender).give(executorArms);
                //返回成功信息
            }else {
                //返回错误信息
                return Command.SINGLE_SUCCESS;
            }
        }else {
            //返回错误信息
            return Command.SINGLE_SUCCESS;
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * 检查发送者和执行者是否均为玩家
     *
     * @param sender   命令发送者
     * @param executor 命令执行者
     * @return 如果发送者和执行者都不是玩家，则返回true，并发送提示信息给发送者；否则返回false
     */
    private static boolean checkIsPlayer(CommandSender sender, Entity executor) {
        if (!((sender instanceof Player) && (executor instanceof Player))) {
            sender.sendMessage(Component.text("该命令只有玩家才可以使用!").color(TextColor.color(0xFF5555)));
            return true;
        }
        return false;
    }

    /**
     * 检查物品是否拥有绑定诅咒附魔
     *
     * @param item   需要检查的物品堆
     * @param sender 命令发送者
     * @return 如果物品拥有绑定诅咒附魔返回true，否则返回false
     */
    private static boolean checkInvEnchant(ItemStack item, CommandSender sender) {
        if (item != null) {
            if (item.getEnchantmentLevel(Enchantment.BINDING_CURSE) != 0) {
                sender.sendMessage(
                        Component.text()
                        .append(item.displayName())
                        .append(Component.text(" 和你的身体粘在了一起，脱不下来...").color(TextColor.color(0xFFFF55)))
                );
                return true;
            }
        }
        return false;
    }

    /**
     * 获取玩家指定位置装备的方法
     *
     * @param inv      玩家的背包
     * @param position 指定的装备位置，有效值包括 "head", "chest", "legs", "feet"
     * @return 对应位置的装备物品栈
     * @throws IllegalArgumentException 如果提供的位置无效时抛出的异常
     */
    private static ItemStack getPlayerArms(PlayerInventory inv, String position) {
        return switch (position) {
            case "head" -> inv.getHelmet();
            case "chest" -> inv.getChestplate();
            case "legs" -> inv.getLeggings();
            case "feet" -> inv.getBoots();
            default -> throw (new IllegalArgumentException("getPlayerArms:IllegalArgumentException:3:" + position));
        };
    }

    /**
     * 设置玩家装备部位
     *
     * @param inv      玩家背包
     * @param item     要设置的物品堆
     * @param position 装备部位，有效值包括 "head", "chest", "legs", "feet"
     * @throws IllegalArgumentException 如果提供的部位无效，抛出异常
     */
    private static void setPlayerArms(PlayerInventory inv, ItemStack item, String position) {
        switch (position) {
            case "head" -> inv.setHelmet(item);
            case "chest" -> inv.setChestplate(item);
            case "legs" -> inv.setLeggings(item);
            case "feet" -> inv.setBoots(item);
            default -> throw (new IllegalArgumentException("setPlayerArms:IllegalArgumentException:3:" + position));
        }
    }
}