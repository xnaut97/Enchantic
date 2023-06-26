package com.github.tezvn.enchantic.api.item;

public interface UpgradeResult {

    int getOldLevel();

    int getNewLevel();

    int getLevel();

    EnchantmentData getEnchantment();

}
