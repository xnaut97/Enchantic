package com.github.tezvn.enchantic.impl.command;

import com.github.tezvn.enchantic.api.EnchanticPlugin;
import com.github.tezvn.enchantic.api.item.EnchanticItem;
import com.github.tezvn.enchantic.impl.gui.ItemBrowserMenu;
import com.github.tezvn.enchantic.impl.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EnchanticCommand extends AbstractCommand {

    public EnchanticCommand(EnchanticPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof ConsoleCommandSender) {
            giveItem(sender, args);
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0)
            return true;
        //enchantic give [player] [id] amount]
        String type = args[0];
        if(type.equalsIgnoreCase("give")) {
            giveItem(sender, args);
        }else if(type.equalsIgnoreCase("items")) {
            openMenu(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0)
            return null;
        if(args.length == 1)
            return Arrays.asList("give", "items");
        String type = args[0];
        if(type.equalsIgnoreCase("give")) {
            if(sender instanceof Player && !sender.hasPermission("enchantic.command.give"))
                return null;
            if(args.length == 2)
                return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName)
                        .filter(name -> name.startsWith(args[1])).collect(Collectors.toList());
            if(args.length == 3)
                return getItemManager().getItems().stream().map(EnchanticItem::getId)
                        .filter(name -> name.startsWith(args[2])).collect(Collectors.toList());
            if(args.length == 4)
                return Collections.singletonList("amount");
        }
        return null;
    }
    
    private void giveItem(CommandSender sender, String[] args) {
        if(sender instanceof Player && !sender.hasPermission("enchantic.command.give")) {
            MessageUtils.sendMessage(sender, "&cYou don't have permission!");
            return;
        }
        if(args.length == 1) {
            MessageUtils.sendMessage(sender, "&cPlease type player name");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if(target == null) {
            MessageUtils.sendMessage(sender, "&cPlayer &6" + args[1] + " is not online!");
            return;
        }
        if(args.length == 2) {
            MessageUtils.sendMessage(sender, "&cPlease type item id");
            return;
        }
        String id = args[2];
        EnchanticItem enchanticItem = getItemManager().getItem(id);
        if(enchanticItem == null) {
            MessageUtils.sendMessage(sender, "&cCould not found item with id &6" + id);
            return;
        }
        int amount = 1;
        if(args.length == 4) {
            try {
                amount = Math.abs(Integer.parseInt(args[3]));
            }catch (Exception e) {
                amount = -1;
            }
        }
        if(amount == -1) {
            MessageUtils.sendMessage(sender, "&cAmount must be a number!");
            return;
        }
        MessageUtils.sendMessage(sender, "&6Giving item to player &a" + target.getName());
        enchanticItem.giveItem(target, amount);
    }

    private void openMenu(CommandSender sender) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(player.hasPermission("enchantic.command.items"))
                new ItemBrowserMenu(0).open(player);
        }
    }
}
