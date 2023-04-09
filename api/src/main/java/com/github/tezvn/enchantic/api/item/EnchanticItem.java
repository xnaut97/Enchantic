package com.github.tezvn.enchantic.api.item;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface EnchanticItem {

    String getId();

    ItemStack getItem();

    List<ItemEnchantment> getAppliedEnchantments();

    List<ItemEnchantment> getByLevel(int level);

    List<ItemEnchantment> getBySuccessRate(double )

    ItemEnchantment getEnchantment(String name);

    boolean hasEnchantment(String name);

    UpgradeResult onUpgrade(ItemStack item);

}
