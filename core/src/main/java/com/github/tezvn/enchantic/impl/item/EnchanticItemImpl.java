package com.github.tezvn.enchantic.impl.item;

import com.github.tezvn.enchantic.api.item.EnchanticItem;
import com.github.tezvn.enchantic.api.item.ItemEnchantment;
import com.github.tezvn.enchantic.api.item.UpgradeResult;
import com.github.tezvn.enchantic.impl.utils.MathUtils;
import com.google.common.collect.Maps;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EnchanticItemImpl implements EnchanticItem {

    private final String id;

    private final ItemStack item;

    private final Map<String, ItemEnchantment> enchantments;

    public EnchanticItemImpl(String id, ItemStack item, Map<String, ItemEnchantment> enchantments) {
        this.id = id;
        this.item = item;
        this.enchantments = enchantments;
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
    public List<ItemEnchantment> getAppliedEnchantments() {
        return Collections.unmodifiableList(new ArrayList<>(this.enchantments.values()));
    }

    @Override
    public List<ItemEnchantment> getByLevel(int level) {
        return getEnchantments(i -> i.getUpgradeLevel() == level);
    }

    @Override
    public List<ItemEnchantment> getBySuccessRate(double successRate) {
        return getEnchantments(i -> i.getSuccessRate() == successRate);
    }

    @Override
    public List<ItemEnchantment> getBySuccessRate(double start, double end) {
        return getEnchantments(i -> i.getSuccessRate() > start && i.getSuccessRate() < end);
    }

    @Override
    public ItemEnchantment getEnchantment(String name) {
        return null;
    }

    @Override
    public boolean hasEnchantment(String name) {
        return false;
    }

    @Override
    public UpgradeResult onUpgrade(ItemStack item) {
        if(item == null || item.getType() == Material.AIR)
            return UpgradeResult.ITEM_NULL_OR_AIR;


        return UpgradeResult.SUCCESS;
    }

    private List<ItemEnchantment> getEnchantments(Predicate<ItemEnchantment> predicate) {
        return getAppliedEnchantments().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private double getRandomChance() {
        return Double.parseDouble(String.valueOf(ThreadLocalRandom.current().nextDouble())
                .substring(0, 6));
    }

}
