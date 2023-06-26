package com.github.tezvn.enchantic.impl.item;

import com.github.tezvn.enchantic.api.item.EnchantmentData;
import com.github.tezvn.enchantic.api.item.UpgradeResult;

public final class UpgradeResultImpl implements UpgradeResult {

    private final int oldLevel;

    private final int newLevel;

    private final int level;

    private final EnchantmentData enchantment;

    UpgradeResultImpl(int oldLevel, int newLevel, int level, EnchantmentData enchantment) {
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.level = level;
        this.enchantment = enchantment;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public int getLevel() {
        return level;
    }

    public EnchantmentData getEnchantment() {
        return enchantment;
    }

}
