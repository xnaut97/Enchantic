package com.github.tezvn.enchantic.impl.gui;

import com.cryptomorin.xseries.XMaterial;
import com.github.tezvn.enchantic.api.EnchanticPlugin;
import com.github.tezvn.enchantic.api.item.EnchanticItem;
import com.github.tezvn.enchantic.impl.utils.ItemCreator;
import com.github.tezvn.enchantic.impl.utils.inventory.BaseInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemBrowserMenu extends BaseInventory.PaginationMenu<EnchanticItem> {

    public ItemBrowserMenu(int page) {
        super(page, 6, "&lITEMS");
    }

    @Override
    public List<EnchanticItem> getObjects() {
        return ((EnchanticPlugin) getPlugin()).getItemManager().getItems();
    }

    @Override
    public InventoryElement getObjectItem(EnchanticItem enchanticItem) {
        ItemCreator creator = new ItemCreator(enchanticItem.getItem().clone());
        creator.insertLore(0,
                "&7❏ Id: &6" + enchanticItem.getId(),
                "&7❏ Applies enchantments: &d" + enchanticItem.getAppliedEnchantments().size(),
                "&7❏ Click:",
                "&7 └ Left: &aGet item",
                "&7 └ Right: &cView enchantments",
                " ",
                "&7Original lore:");
        return new InventoryElement(creator.build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                switch (event.getClick()) {
                    case LEFT:
                        enchanticItem.giveItem(player, 1);
                        break;
                    case RIGHT:
                        open(player, false, new EnchantmentBrowserMenu(enchanticItem, true));
                        break;
                }
            }
        };
    }

    @Override
    public ItemStack fillOtherSlotWhenFull() {
        return XMaterial.GRAY_STAINED_GLASS_PANE.parseItem();
    }
    
}
