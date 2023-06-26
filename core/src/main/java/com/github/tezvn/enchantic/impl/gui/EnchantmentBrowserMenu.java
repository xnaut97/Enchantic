package com.github.tezvn.enchantic.impl.gui;

import com.cryptomorin.xseries.XMaterial;
import com.github.tezvn.enchantic.api.item.EnchanticItem;
import com.github.tezvn.enchantic.api.item.EnchantmentType;
import com.github.tezvn.enchantic.api.item.LevelType;
import com.github.tezvn.enchantic.api.item.EnchantmentData;
import com.github.tezvn.enchantic.impl.utils.ItemCreator;
import com.github.tezvn.enchantic.impl.utils.MessageUtils;
import com.github.tezvn.enchantic.impl.utils.inventory.BaseInventory;
import net.advancedplugins.ae.api.AEAPI;
import net.advancedplugins.ae.enchanthandler.enchantments.AdvancedEnchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class EnchantmentBrowserMenu extends BaseInventory.PaginationMenu<EnchantmentData> {

    private final EnchanticItem enchanticItem;

    private final boolean openFromMenu;

    private final int browserPage;

    public EnchantmentBrowserMenu(EnchanticItem enchanticItem, boolean openFromMenu) {
        this(0, enchanticItem, openFromMenu);
    }

    public EnchantmentBrowserMenu(int browserPage, EnchanticItem enchanticItem, boolean openFromMenu) {
        super(0, 6, "&lCÁC PHÙ PHÉP ĐƯỢC ÁP DỤNG");
        this.openFromMenu = openFromMenu;
        this.enchanticItem = enchanticItem;
        this.browserPage = browserPage;
        setupExitButton();
    }



    private void setupExitButton() {
        if (this.openFromMenu)
            pushElement(45, new InventoryElement(new ItemCreator(Objects.requireNonNull(XMaterial.OAK_DOOR.parseItem()))
                    .setDisplayName("&6&lEXIT")
                    .addLore("&7Click to go to item menu.")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    event.setCancelled(true);
                    Player player = (Player) event.getWhoClicked();
                    new ItemBrowserMenu(browserPage).open(player);
                }
            });
    }

    @Override
    public List<EnchantmentData> getObjects() {
        return enchanticItem.getAppliedEnchantments();
    }

    @Override
    public InventoryElement getObjectItem(EnchantmentData enchantmentData) {
        int type = getEnchantmentType(enchantmentData);
        ItemCreator creator = new ItemCreator(Objects.requireNonNull(
                enchantmentData.getType() == EnchantmentType.VANILLA
                        ? XMaterial.BOOK.parseItem() : XMaterial.ENCHANTED_BOOK.parseItem()));
        String name = "";
        switch (type) {
            case 1:
                name = "&b&l" + MessageUtils.capitalize(enchantmentData.getName(), MessageUtils.CapitalizeMode.FIRST);
                break;
            case 2:
                AdvancedEnchantment enchantment = AEAPI.getEnchantmentInstance(enchantmentData.getName());
                name = enchantment.getDisplay().replace("%group-color%", enchantment.getGroup().getColor());
                break;
        }
        creator.setDisplayName(name);
        if(getPlayer().hasPermission("enchantic.admin.view"))
            creator.addLore("&7❏ Loại: &" + (enchantmentData.getType() == EnchantmentType.VANILLA ? "&a&l" : "&6&l")
                    + enchantmentData.getType().name());
        String upgradeLevel;
        int lowestUpgradeLevel = enchantmentData.getLevel(LevelType.LOWEST_UPGRADE_LEVEL);
        int highestUpgradeLevel = enchantmentData.getLevel(LevelType.HIGHEST_UPGRADE_LEVEL);
        if(lowestUpgradeLevel == highestUpgradeLevel)
            upgradeLevel = String.valueOf(lowestUpgradeLevel);
        else
            upgradeLevel = lowestUpgradeLevel + "-" + highestUpgradeLevel;
        
        String downgradeLevel = null;
        int lowestDowngradeLevel = enchantmentData.getLevel(LevelType.LOWEST_DOWNGRADE_LEVEL);
        int highestDowngradeLevel = enchantmentData.getLevel(LevelType.HIGHEST_DOWNGRADE_LEVEL);
        if(lowestDowngradeLevel > 0 && highestDowngradeLevel > 0) {
            if (lowestDowngradeLevel == highestDowngradeLevel)
                downgradeLevel = String.valueOf(lowestDowngradeLevel);
            else
                downgradeLevel = lowestDowngradeLevel + "-" + highestDowngradeLevel;
        }
        String realSuccessRate = String.valueOf(enchantmentData.getSuccessRate()*100);
        if(realSuccessRate.length() > 4)
            realSuccessRate = realSuccessRate.substring(0, 4);
        String realFailureRate = String.valueOf(enchantmentData.getFailureRate()*100);
        if(realFailureRate.length() > 4)
            realFailureRate = realFailureRate.substring(0, 4);
        creator.addLore(
                "&7❏ Cấp độ ép tối đa: &e" + enchantmentData.getLevel(LevelType.MAX_UPGRADE_LEVEL) + " &7cấp",
                "&7❏ Tỉ lệ ép:",
                "&7 └ Thành công: &a" + realSuccessRate + "%",
                "&7   ▲ Tăng cấp: &a" + upgradeLevel + " &7cấp",
                "&7 └ Thất bại: &c" + realFailureRate + "%");
        if(downgradeLevel != null)
            creator.addLore("&7   ▼ Giảm cấp: &c" + downgradeLevel + " &7cấp");
        return new InventoryElement(creator.build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
            }
        };
    }

    @Override
    public ItemStack fillOtherSlotWhenFull() {
        return XMaterial.GRAY_STAINED_GLASS_PANE.parseItem();
    }

    @Override
    public boolean onValidate(EnchantmentData object) {
        return object.getType() != EnchantmentType.UNDEFINED;
    }

    private int getEnchantmentType(EnchantmentData enchantment) {
        String name = enchantment.getName();
        if(Enchantment.getByKey(new NamespacedKey("minecraft", name)) != null)
            return 1;
        else if(AEAPI.isAnEnchantment(name))
            return 2;
        return 0;
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
