package com.github.tezvn.enchantic.impl.item;

import com.github.tezvn.enchantic.api.item.EnchanticItem;
import com.github.tezvn.enchantic.api.item.UpgradeResult;
import com.github.tezvn.enchantic.impl.utils.MathUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnchanticItemImpl implements EnchanticItem {

    private final String id;

    private final ItemStack item;

    private final int levelUp;

    private final int maxLevel;

    private final double successRate;

    public EnchanticItemImpl(String id, ItemStack item, int levelUp, int maxLevel, double successRate) {
        this.id = id;
        this.item = item;
        this.levelUp = levelUp;
        this.maxLevel = maxLevel;
        this.successRate = MathUtils.roundDouble(successRate/100, 4);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ItemStack getItem() {
        return this.item;
    }

    @Override
    public int getLevelUp() {
        return this.levelUp;
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public double getSuccessRate() {
        return this.successRate;
    }

    @Override
    public double getFailureRate() {
        return MathUtils.roundDouble(1 - this.getSuccessRate(), 4);
    }

    @Override
    public List<String> getApplyEnchants() {
        return null;
    }

    @Override
    public UpgradeResult onUpgrade(ItemStack item) {
        if(item == null || item.getType() == Material.AIR)
            return UpgradeResult.ITEM_NULL_OR_AIR;


        return UpgradeResult.SUCCESS;
    }

    private double getRandomChance() {
        return Double.parseDouble(String.valueOf(ThreadLocalRandom.current().nextDouble())
                .substring(0, 6));
    }

}
