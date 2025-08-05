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

public class PutCommand {

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

    private static boolean checkIsPlayer(CommandSender sender, Entity executor) {
        if (!((sender instanceof Player) && (executor instanceof Player))) {
            sender.sendMessage(Component.text("该命令只有玩家才可以使用!").color(TextColor.color(0xFF5555)));
            return true;
        }
        return false;
    }

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

    private static ItemStack getPlayerArms(PlayerInventory inv, String position) {
        return switch (position) {
            case "head" -> inv.getHelmet();
            case "chest" -> inv.getChestplate();
            case "legs" -> inv.getLeggings();
            case "feet" -> inv.getBoots();
            default -> throw (new IllegalArgumentException("getPlayerArms:IllegalArgumentException:3:" + position));
        };
    }

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