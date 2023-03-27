package xyz.hsbestudio.tools.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ArmorUtils {
    public static boolean checkDurability(ItemStack i, double threshold) {
        return getDamage(i) <= threshold;
    }

    public static double getDamage(ItemStack i) {
        return (((double) (i.getMaxDamage() - i.getDamage()) / i.getMaxDamage()) * 100);
    }

    public static ItemStack getArmor(int slot) {
        if (mc.player == null) return null;

        return mc.player.getInventory().armor.get(slot);
    }

    public static boolean isHelmet(ItemStack itemStack) {
        if (itemStack == null) return false;

        return itemStack.getItem() == Items.LEATHER_HELMET || itemStack.getItem() == Items.NETHERITE_HELMET
            || itemStack.getItem() == Items.DIAMOND_HELMET || itemStack.getItem() == Items.GOLDEN_HELMET
            || itemStack.getItem() == Items.IRON_HELMET || itemStack.getItem() == Items.CHAINMAIL_HELMET;
    }

    public static boolean isChestPlate(ItemStack itemStack) {
        if (itemStack == null) return false;

        return itemStack.getItem() == Items.LEATHER_CHESTPLATE || itemStack.getItem() == Items.NETHERITE_CHESTPLATE
            || itemStack.getItem() == Items.DIAMOND_CHESTPLATE || itemStack.getItem() == Items.GOLDEN_CHESTPLATE
            || itemStack.getItem() == Items.IRON_CHESTPLATE || itemStack.getItem() == Items.CHAINMAIL_CHESTPLATE;
    }

    public static boolean isLeggings(ItemStack itemStack) {
        if (itemStack == null) return false;

        return itemStack.getItem() == Items.LEATHER_LEGGINGS || itemStack.getItem() == Items.NETHERITE_LEGGINGS
            || itemStack.getItem() == Items.DIAMOND_LEGGINGS || itemStack.getItem() == Items.GOLDEN_LEGGINGS
            || itemStack.getItem() == Items.IRON_LEGGINGS || itemStack.getItem() == Items.CHAINMAIL_LEGGINGS;
    }

    public static boolean isBoots(ItemStack itemStack) {
        if (itemStack == null) return false;

        return itemStack.getItem() == Items.LEATHER_BOOTS || itemStack.getItem() == Items.NETHERITE_BOOTS
            || itemStack.getItem() == Items.DIAMOND_BOOTS || itemStack.getItem() == Items.GOLDEN_BOOTS
            || itemStack.getItem() == Items.IRON_BOOTS || itemStack.getItem() == Items.CHAINMAIL_BOOTS;
    }
}
