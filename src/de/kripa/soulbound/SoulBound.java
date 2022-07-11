package de.kripa.soulbound;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SoulBound extends JavaPlugin implements Listener, CommandExecutor {
    public static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + "SoulBound" + ChatColor.GRAY + "] " + ChatColor.WHITE;
    private Table<Player, ItemStack, Integer> soulBoundItems = HashBasedTable.create();

    @Override
    public void onEnable() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        getCommand("soulbind").setExecutor(this);

        FileConfiguration config = getConfig();

    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!p.hasPermission("soulbound.soulbind")) {
                return true;
            }
            ItemStack hand = p.getInventory().getItemInMainHand();
            ItemMeta handMeta = hand.getItemMeta();
            handMeta.setLore(List.of("§7Soulbound"));
            hand.setItemMeta(handMeta);
            p.getInventory().setItemInMainHand(hand);
        } else {
            sender.sendMessage(PREFIX + "You can only use this command as player!");
        }
        return true;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) {
                continue;
            }
            if (item.getItemMeta().getLore() == null) {
                continue;
            }
            if (item.getItemMeta().getLore().contains("§7Soulbound")) {
                soulBoundItems.put(p, item, i);
                e.getDrops().remove(item);
            }
        }
        p.sendMessage(PREFIX + "Death Position: §c" + p.getLocation().getBlockX() + " " + p.getLocation().getBlockY() + " " + p.getLocation().getBlockZ());

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {

        Player p = e.getPlayer();
        for (Table.Cell<Player, ItemStack, Integer> soulBoundItem : soulBoundItems.cellSet()) {
            if (soulBoundItem.getRowKey() == p) {
                p.getInventory().setItem(soulBoundItem.getValue(), soulBoundItem.getColumnKey());
            }
        }
        p.sendMessage(PREFIX + "§aRestored Items successfully");
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        if (inv.getItem(0) == null || inv.getItem(1) == null) {
            return;
        }

        if (inv.getItem(1).getType() != Material.COMPASS) {
            return;
        }
        ItemStack result = inv.getItem(0).clone();
        ItemMeta resultMeta = result.getItemMeta();
        resultMeta.setLore(List.of("§7Soulbound"));
        result.setItemMeta(resultMeta);
        getServer().getScheduler().runTask(this, () -> {
            e.getView().setProperty(InventoryView.Property.REPAIR_COST, 2);
        });
        e.setResult(result);
    }
}
