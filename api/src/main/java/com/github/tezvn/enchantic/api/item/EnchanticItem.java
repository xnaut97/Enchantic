package com.github.tezvn.enchantic.api.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public interface EnchanticItem {

    String getId();

    ItemStack getItem();

    List<EnchantmentData> getAppliedEnchantments();

    List<EnchantmentData> getByLevel(int level);

    List<EnchantmentData> getBySuccessRate(double successRate);

    List<EnchantmentData> getBySuccessRate(double start, double end);

    @Nullable
    EnchantmentData getEnchantment(String name);

    boolean hasEnchantment(String name);

    boolean canUpgrade(ItemStack item);

    boolean isReachedMax(ItemStack item);

    List<UpgradeResult> onUpgrade(ItemStack item);

    void giveItem(Player player, int amount);

}
