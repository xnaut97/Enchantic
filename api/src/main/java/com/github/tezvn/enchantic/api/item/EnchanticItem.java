package com.github.tezvn.enchantic.api.item;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface EnchanticItem {

    String getId();

    ItemStack getItem();

    List<ItemEnchantment> getAppliedEnchantments();

    List<ItemEnchantment> getByLevel(int level);

    List<ItemEnchantment> getBySuccessRate(double successRate);

    List<ItemEnchantment> getBySuccessRate(double start, double end);

    ItemEnchantment getEnchantment(String name);

    boolean hasEnchantment(String name);

    UpgradeResult onUpgrade(ItemStack item);

}
