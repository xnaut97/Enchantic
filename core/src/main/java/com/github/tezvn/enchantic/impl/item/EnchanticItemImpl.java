package com.github.tezvn.enchantic.impl.item;

import com.github.tezvn.enchantic.api.item.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.tr7zw.nbtapi.NBTItem;
import net.advancedplugins.ae.api.AEAPI;
import net.advancedplugins.ae.enchanthandler.enchantments.AdvancedEnchantment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EnchanticItemImpl implements EnchanticItem {

    private final String id;

    private final ItemStack item;

    private final Map<String, EnchantmentData> enchantments = Maps.newHashMap();

    public EnchanticItemImpl(String id, ItemStack item, Map<String, EnchantmentData> enchantments) {
        this.id = id;
        this.item = item;
        this.enchantments.putAll(enchantments);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ItemStack getItem() {
        ItemStack clone = this.item.clone();
        NBTItem nbt = new NBTItem(clone, true);
        nbt.setString("enchantic_id", getId());
        return clone;
    }

    @Override
    public List<EnchantmentData> getAppliedEnchantments() {
        return Collections.unmodifiableList(new ArrayList<>(this.enchantments.values()));
    }

    @Override
    public List<EnchantmentData> getByLevel(int level) {
        return getEnchantments(i -> {
            int lowLevel = i.getLevel(LevelType.LOWEST_UPGRADE_LEVEL);
            int highLevel = i.getLevel(LevelType.HIGHEST_UPGRADE_LEVEL);
            return lowLevel == highLevel ? lowLevel == level : lowLevel < level && level < highLevel;
        });
    }

    @Override
    public List<EnchantmentData> getBySuccessRate(double successRate) {
        return getEnchantments(i -> i.getSuccessRate() == successRate);
    }

    @Override
    public List<EnchantmentData> getBySuccessRate(double start, double end) {
        return getEnchantments(i -> i.getSuccessRate() > start && i.getSuccessRate() < end);
    }

    @Override
    public EnchantmentData getEnchantment(String name) {
        return this.enchantments.getOrDefault(name, null);
    }

    @Override
    public boolean hasEnchantment(String name) {
        return getEnchantment(name) != null;
    }

    @Override
    public boolean canUpgrade(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return false;
        ItemMeta meta = item.getItemMeta();
        List<EnchantmentData> list = Lists.newArrayList();
        if (meta != null) {
            meta.getEnchants().keySet().forEach(e -> {
                EnchantmentData data = getEnchantment(e.getKey().getKey());
                if (data != null)
                    list.add(data);
            });
        }
        AEAPI.getEnchantmentsOnItem(item).keySet().forEach(e -> {
            EnchantmentData data = getEnchantment(e);
            if (data != null)
                list.add(data);
        });
        return list.size() > 0;
    }

    @Override
    public boolean isReachedMax(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return false;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                EnchantmentData data = getEnchantment(entry.getKey().getKey().getKey());
                if (data == null)
                    continue;
                int highest = data.getLevel(LevelType.MAX_UPGRADE_LEVEL);
                if(entry.getValue() >= highest)
                    continue;
                return false;
            }
        }
        for (Map.Entry<String, Integer> entry : AEAPI.getEnchantmentsOnItem(item).entrySet()) {
            EnchantmentData data = getEnchantment(entry.getKey());
            if (data == null)
                continue;
            AdvancedEnchantment ae = AEAPI.getEnchantmentInstance(data.getName());
            int highest = Math.min(data.getLevel(LevelType.MAX_UPGRADE_LEVEL), ae.getHighestLevel());
            if (entry.getValue() >= highest)
                continue;
            return false;
        }
        return true;
    }

    @Override
    public List<UpgradeResult> onUpgrade(ItemStack item) {
        List<UpgradeResult> results = Lists.newArrayList();
        if (item == null || item.getType() == Material.AIR)
            return results;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getEnchants().forEach((enchant, integer) -> {
                UpgradeResult upgrade = applyUpgrade(enchant.getKey().getKey(), integer);
                if (upgrade == null)
                    return;
                EnchantmentData enchantment = upgrade.getEnchantment();
                if (integer >= enchantment.getLevel(LevelType.MAX_UPGRADE_LEVEL)
                        || (integer == 1 && upgrade.getNewLevel() < 1)
                        || upgrade.getOldLevel() == upgrade.getNewLevel())
                    return;
                int toApply = Math.max(1, Math.min(enchantment.getLevel(LevelType.MAX_UPGRADE_LEVEL), upgrade.getNewLevel()));
                meta.addEnchant(enchant, toApply, true);
                results.add(upgrade);
            });
        }
        item.setItemMeta(meta);
        for (Map.Entry<String, Integer> entry : AEAPI.getEnchantmentsOnItem(item).entrySet()) {
            String s = entry.getKey();
            int integer = entry.getValue();
            UpgradeResult upgrade = applyUpgrade(s, integer);
            if (upgrade == null)
                continue;
            EnchantmentData enchantment = upgrade.getEnchantment();
            AdvancedEnchantment enchant = AEAPI.getEnchantmentInstance(s);
            List<Integer> levels = enchant.getLevelList();
            int lowest = levels.get(0);
            int highest = Math.min(enchant.getHighestLevel(), enchantment.getLevel(LevelType.MAX_UPGRADE_LEVEL));
            int toApply = Math.max(lowest, Math.min(highest, upgrade.getNewLevel()));
            if (integer >= highest || (integer == 1 && upgrade.getNewLevel() < 1)
                    || upgrade.getOldLevel() == upgrade.getNewLevel())
                continue;
            ItemStack modified = AEAPI.applyEnchant(s, toApply, item);
            item.setItemMeta(modified.getItemMeta());
            NBTItem nbt = new NBTItem(item, true);
            nbt.mergeCustomNBT(modified);
            results.add(upgrade);
        }
        return results;
    }

    @Override
    public void giveItem(Player player, int amount) {
        IntStream.range(0, amount).forEach(i -> {
            if (!player.isOnline())
                return;
            player.getInventory().addItem(getItem()).forEach((integer, itemStack) -> {
                player.getWorld().dropItem(player.getLocation(), item);
            });
        });
    }

    private UpgradeResult applyUpgrade(String s, int integer) {
        EnchantmentData enchantment = getEnchantment(s);
        if (enchantment == null)
            return null;
        double randomChance = getRandomChance();
        int level;
        int random;
        if (randomChance > enchantment.getSuccessRate() && enchantment.getLevel(LevelType.HIGHEST_DOWNGRADE_LEVEL) > 0)
            level = integer - (random = getRandomDowngradeLevel(enchantment));
        else
            level = integer + (random = getRandomUpgradeLevel(enchantment));
        return new UpgradeResultImpl(integer, level, random, enchantment);
    }

    private List<EnchantmentData> getEnchantments(Predicate<EnchantmentData> predicate) {
        return getAppliedEnchantments().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private double getRandomChance() {
        return Double.parseDouble(String.valueOf(ThreadLocalRandom.current().nextDouble())
                .substring(0, 6));
    }

    private int getRandomUpgradeLevel(EnchantmentData enchantment) {
        int low = enchantment.getLevel(LevelType.LOWEST_UPGRADE_LEVEL);
        int high = enchantment.getLevel(LevelType.HIGHEST_UPGRADE_LEVEL);
        return low == high ? low : ThreadLocalRandom.current().nextInt(low, high);
    }

    private int getRandomDowngradeLevel(EnchantmentData enchantment) {
        int low = enchantment.getLevel(LevelType.LOWEST_DOWNGRADE_LEVEL);
        int high = enchantment.getLevel(LevelType.HIGHEST_DOWNGRADE_LEVEL);
        return low == high ? low : ThreadLocalRandom.current().nextInt(low, high);
    }

}
