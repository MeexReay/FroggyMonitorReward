package ru.froggymonitor.rewardplugin;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class Reward {
    public String name;
    public Map<String,Object> data;

    public Reward(String name, Map<String,Object> data) {
        this.data = data;
        this.name = name;
    }

    public void execute(String nickname) {
        Player player = Bukkit.getPlayer(nickname);

        if (player != null) {
            if (data.containsKey("item")) {
                String[] ss = ((String)data.get("item")).split(" ");
                ItemStack item = new ItemStack(Material.valueOf(ss[0].toUpperCase()), ss.length == 1 ? 1 : Integer.parseInt(ss[1]));
                player.getInventory().addItem(item);
            }
            if (data.containsKey("message")) {
                player.spigot().sendMessage(Main.me.message_formatting.format(PlaceholderAPI.setPlaceholders(player, (String) data.get("message"))));
            }
            Main.me.cache.put(nickname, name);
        }

        OfflinePlayer offlinePlayer = player != null ? player : Main.getOfflinePlayer(nickname);

        if (offlinePlayer != null) {
            if (data.containsKey("vault")) {
                Main.me.econ.depositPlayer(offlinePlayer,
                    ((Number) data.get("vault")).doubleValue());
            }
            if (data.containsKey("commands")) {
                for (String c : new ArrayList<>((List<String>) data.get("commands"))) {
                    if (c.startsWith("/")) c = c.substring(1);
                    getServer().dispatchCommand(getServer().getConsoleSender(),PlaceholderAPI.setPlaceholders(offlinePlayer, c));
                }
            }
        }
    }

    public void later(Player player) {
        if (data.containsKey("item")) {
            String[] ss = ((String)data.get("item")).split(" ");
            ItemStack item = new ItemStack(Material.valueOf(ss[0].toUpperCase()), ss.length == 1 ? 1 : Integer.parseInt(ss[1]));
            player.getInventory().addItem(item);
        }
        if (data.containsKey("message")) {
            player.spigot().sendMessage(Main.me.message_formatting.format(PlaceholderAPI.setPlaceholders(player, (String) data.get("message"))));
        }
    }
}
