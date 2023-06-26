package com.github.tezvn.enchantic.impl.item;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.github.tezvn.enchantic.api.EnchanticPlugin;
import com.github.tezvn.enchantic.api.item.*;
import com.github.tezvn.enchantic.api.item.UpgradeResult;
import com.github.tezvn.enchantic.impl.gui.EnchantmentBrowserMenu;
import com.github.tezvn.enchantic.impl.utils.ItemCreator;
import com.github.tezvn.enchantic.impl.utils.MathUtils;
import com.github.tezvn.enchantic.impl.utils.MessageUtils;
import com.github.tezvn.enchantic.impl.utils.RomanNumerals;
import de.tr7zw.nbtapi.NBTItem;
import net.advancedplugins.ae.api.AEAPI;
import net.advancedplugins.ae.enchanthandler.enchantments.AdvancedEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ItemManagerImpl implements ItemManager, Listener {

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    private final ConcurrentHashMap<String, EnchanticItem> items = new ConcurrentHashMap<>();

    private final EnchanticPlugin plugin;

    public ItemManagerImpl(EnchanticPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public EnchanticPlugin getPlugin() {
        return plugin;
    }

    @Override
    public List<EnchanticItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(this.items.values()));
    }

    @Override
    @Nullable
    public EnchanticItem getItem(String id) {
        return this.items.getOrDefault(id, null);
    }

    public void load() {
        File file = new File(plugin.getDataFolder() + "/items.yml");
        if (!file.exists()) {
            plugin.getLogger().severe("Could not load items due to 'items.yml' not found");
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.getKeys(false).forEach(id -> {
            Future<EnchanticItem> future = executor.submit(new Callable<EnchanticItem>() {
                @Override
                public EnchanticItem call() throws Exception {
                    XMaterial material = XMaterial.matchXMaterial(config.getString(id + ".material", "STICK"))
                            .orElse(XMaterial.STICK);
                    ItemStack i = Objects.requireNonNull(XMaterial.matchXMaterial(
                            config.getString(id + ".material", "STICK")).orElse(XMaterial.STICK).parseItem());
                    ItemCreator creator = new ItemCreator(i);
                    if (material == XMaterial.PLAYER_HEAD)
                        creator.setTexture(config.getString(id + ".texture", ""));
                    creator.setDisplayName(config.getString(id + ".name", MessageUtils.format(i.getType())));
                    creator.setLore(config.getStringList(id + ".lore"));
                    creator.setGlow(config.getBoolean(id + ".glow", false));
                    ConfigurationSection enchantSection = config.getConfigurationSection(id + ".applies-enchants");
                    ConcurrentHashMap<String, EnchantmentData> map = new ConcurrentHashMap<>();
                    if (enchantSection != null) {
                        enchantSection.getKeys(false).forEach(name -> {
                            String section = id + ".applies-enchants." + name;
                            String rate = config.getString(section + ".success-rate", "1%")
                                    .replace("%", "");
                            double successRate = MathUtils.convertToPercent(Double.parseDouble(rate), 2);
                            ConcurrentHashMap<LevelType, Integer> levels = new ConcurrentHashMap<>();
                            levels.put(LevelType.MAX_UPGRADE_LEVEL, Math.abs(
                                    config.getInt(section + ".level.max", 100)));
                            String levelUpgrade = config.getString(section + ".level.per-upgrade", "1");
                            int lowestUpgradeLevel, highestUpgradeLevel;
                            if (levelUpgrade.contains("-")) {
                                String[] split = levelUpgrade.split("-");
                                lowestUpgradeLevel = Integer.parseInt(split[0]);
                                highestUpgradeLevel = Integer.parseInt(split[1]);
                            } else
                                lowestUpgradeLevel = highestUpgradeLevel = Integer.parseInt(levelUpgrade);
                            levels.put(LevelType.LOWEST_UPGRADE_LEVEL, lowestUpgradeLevel);
                            levels.put(LevelType.HIGHEST_UPGRADE_LEVEL, highestUpgradeLevel);
                            String levelDowngrade = config.getString(section + ".downgrade-level-on-failed", "0");
                            int lowestDowngradeLevel, highestDowngradeLevel;
                            if (levelDowngrade.contains("-")) {
                                String[] split = levelUpgrade.split("-");
                                lowestDowngradeLevel = Integer.parseInt(split[0]);
                                highestDowngradeLevel = Integer.parseInt(split[1]);
                            } else
                                lowestDowngradeLevel = highestDowngradeLevel = Integer.parseInt(levelDowngrade);
                            levels.put(LevelType.LOWEST_DOWNGRADE_LEVEL, lowestDowngradeLevel);
                            levels.put(LevelType.HIGHEST_DOWNGRADE_LEVEL, highestDowngradeLevel);
                            EnchantmentData enchantmentData = new EnchantmentDataImpl(
                                    name.toLowerCase(), successRate, levels);
                            if (enchantmentData.getType() == EnchantmentType.UNDEFINED) {
                                plugin.getLogger().severe("&cCould not load undefined enchantment with name '" + name + "'"
                                        + "it is not a valid vanilla/advanced enchantment, skipped!");
                                return;
                            }
                            map.put(name.toLowerCase(), enchantmentData);
                        });
                    }
                    return new EnchanticItemImpl(id, creator.build(), map);
                }
            });
            try {
                EnchanticItem enchanticItem = future.get();
                if (this.items.putIfAbsent(enchanticItem.getId(), enchanticItem) != null)
                    plugin.getLogger().severe("Found duplicate item with id '" + id + "', skipped!");
            } catch (InterruptedException | ExecutionException e) {
                plugin.getLogger().severe("Error while loading item with id '" + id + "'");
            }
        });
        if (!this.items.isEmpty())
            plugin.getLogger().info("Loaded " + items.size() + " items from config.");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        Inventory inventory = event.getClickedInventory();
        if (inventory == null)
            return;
        if (event.getClick() == ClickType.CREATIVE)
            return;
        if (!checkValid(item) || !checkValid(cursor))
            return;
        NBTItem nbt = new NBTItem(cursor);
        if (!nbt.hasTag("enchantic_id"))
            return;
        String id = nbt.getString("enchantic_id");
        EnchanticItem enchanticItem = getItem(id);
        if (enchanticItem == null)
            return;
        event.setCancelled(true);
        ItemStack clone = item.clone();
        if (!enchanticItem.canUpgrade(clone)) {
            MessageUtils.sendMessage(player, "&cVật phẩm này không có phù phép nào phù hợp để ép, vui lòng thử lại!");
            XSound.ENTITY_BLAZE_DEATH.play(player, .75f, -1f);
            return;
        }
        if(enchanticItem.isReachedMax(clone)) {
            MessageUtils.sendMessage(player, "&cTất cả cường hóa đã đạt cấp tối đa, không thể ép nữa!");
            XSound.ENTITY_VILLAGER_NO.play(player, .75f, -.5f);
            return;
        }
        List<UpgradeResult> result = enchanticItem.onUpgrade(clone);
        if (result.isEmpty()) {
            MessageUtils.sendMessage(player, "&cKhông có cường hóa nào thay đổi, xin thử lại lần sau.");
            XSound.ENTITY_BLAZE_DEATH.play(player, .75f, 0f);
        }else {
            event.setCurrentItem(clone);
            XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
        }
        if (!player.hasPermission("enchantic.reinforce.bypass")) {
            cursor.setAmount(cursor.getAmount() - 1);
            player.setItemOnCursor(cursor);
        }
        List<UpgradeResult> successResult = result.stream().filter(i -> {
                    int oldLevel = i.getOldLevel();
                    int newLevel = i.getNewLevel();
                    EnchantmentData data = i.getEnchantment();
                    int highest = data.getLevel(LevelType.MAX_UPGRADE_LEVEL);
                    if (data.getType() == EnchantmentType.ADVANCED_ENCHANTMENTS) {
                        AdvancedEnchantment ae = AEAPI.getEnchantmentInstance(data.getName());
                        highest = Math.min(highest, ae.getHighestLevel());
                    }
                    return oldLevel < highest && newLevel > oldLevel;
                })
                .collect(Collectors.toList());
        if (successResult.size() > 0) {
            MessageUtils.sendMessage(player, "&6Các cường hóa thành công");
            successResult.forEach(i -> {
                int oldLevel = i.getOldLevel();
                int newLevel = i.getNewLevel();
                EnchantmentData data = i.getEnchantment();
                int highest = data.getLevel(LevelType.MAX_UPGRADE_LEVEL);
                if (data.getType() == EnchantmentType.ADVANCED_ENCHANTMENTS) {
                    AdvancedEnchantment ae = AEAPI.getEnchantmentInstance(data.getName());
                    highest = Math.min(highest, ae.getHighestLevel());
                }
                String modified = "&a+" + (i.getOldLevel() + i.getLevel() >= highest ? highest - i.getOldLevel() : i.getLevel());
                String reachStatus = i.getLevel() + i.getOldLevel() >= highest ? " &7[&aĐẠT CẤP TỐI ĐA&7]" : "";
                String name = MessageUtils.capitalize(data.getName(), MessageUtils.CapitalizeMode.FIRST);
                MessageUtils.sendMessage(player,
                        "&f- &6" + name + ": &f" + RomanNumerals.toRomanNumeral(oldLevel)
                                + " » &e"
                                + RomanNumerals.toRomanNumeral(Math.min(newLevel, highest))
                                + " &7(" + modified + "&7)" + reachStatus);
            });
        }

        List<UpgradeResult> failedResult = result.stream().filter(i -> {
                    int oldLevel = i.getOldLevel();
                    int newLevel = i.getNewLevel();
                    return oldLevel > 1 && oldLevel > newLevel;
                })
                .collect(Collectors.toList());
        if (failedResult.size() > 0) {
            MessageUtils.sendMessage(player, "&6Các cường hóa thất bại");
            failedResult.forEach(i -> {
                int oldLevel = i.getOldLevel();
                int newLevel = i.getNewLevel();
                EnchantmentData data = i.getEnchantment();
                String modified = "&c-" + (i.getNewLevel() <= 1 ? Math.abs(1 - i.getOldLevel()) : i.getLevel());
                String reachStatus = i.getOldLevel() - i.getLevel() <= 1 ? " &7[&cĐẠT CẤP TỐI THIỂU&7]" : "";
                String name = MessageUtils.capitalize(data.getName(), MessageUtils.CapitalizeMode.FIRST);
                MessageUtils.sendMessage(player,
                        "&f- &6" + name + ": &f" + RomanNumerals.toRomanNumeral(oldLevel)
                                + " » &e"
                                + RomanNumerals.toRomanNumeral(Math.max(1, newLevel))
                                + " &7(" + modified + "&7)" + reachStatus);
            });
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();
        if (!action.name().contains("RIGHT_CLICK_"))
            return;
        if (!checkValid(item))
            return;
        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasTag("enchantic_id"))
            return;
        String id = nbt.getString("enchantic_id");
        EnchanticItem enchanticItem = getItem(id);
        if (enchanticItem == null)
            return;
        new EnchantmentBrowserMenu(enchanticItem, false).open(player);
        XSound.ITEM_BOOK_PAGE_TURN.play(player);
    }

    private boolean checkValid(ItemStack item) {
        return item != null && item.getType() != Material.AIR && item.getAmount() > 0;
    }

}
