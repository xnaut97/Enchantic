package com.github.tezvn.enchantic.impl.item;

import com.github.tezvn.enchantic.api.item.EnchantmentType;
import com.github.tezvn.enchantic.api.item.LevelType;
import com.github.tezvn.enchantic.api.item.EnchantmentData;
import net.advancedplugins.ae.api.AEAPI;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentDataImpl implements EnchantmentData {

    private final String name;

    private final double successRate;

    private final ConcurrentHashMap<LevelType, Integer> levels = new ConcurrentHashMap<>();

    public EnchantmentDataImpl(String name, double successRate, ConcurrentHashMap<LevelType, Integer> levels) {
        this.name = name == null ? "" : name;
        this.successRate = successRate;
        this.levels.putAll(levels);
    }

    @Override
    public EnchantmentType getType() {
        if(Enchantment.getByKey(new NamespacedKey("minecraft", getName())) != null)
            return EnchantmentType.VANILLA;
        else if(AEAPI.isAnEnchantment(getName()))
            return EnchantmentType.ADVANCED_ENCHANTMENTS;
        return EnchantmentType.UNDEFINED;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public double getSuccessRate() {
        return this.successRate;
    }

    @Override
    public double getFailureRate() {
        return 1 - getSuccessRate();
    }

    @Override
    public int getLevel(LevelType level) {
        return this.levels.getOrDefault(level, 0);
    }

}
