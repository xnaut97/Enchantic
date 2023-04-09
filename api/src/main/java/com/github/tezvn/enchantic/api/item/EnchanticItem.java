package com.github.tezvn.enchantic.api.item;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public interface EnchanticItem {

    String getId();

    ItemStack getItem();

    List<ItemEnchantment> getAppliedEnchantments();

    List<ItemEnchantment> getByLevel(int level);

    List<ItemEnchantment> getBySuccessRate(double successRate);

    List<ItemEnchantment> getBySuccessRate(double start, double end);

    @Nullable
    ItemEnchantment getEnchantment(String name);

    boolean hasEnchantment(String name);

    UpgradeResult onUpgrade(ItemStack item);

}
