package ru.froggymonitor.rewardplugin;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

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
        new BukkitRunnable() {
            public void run() {
//                System.out.println("nickname "+nickname);

                Player player = Bukkit.getPlayer(nickname);

//                System.out.println("player "+player);

//                System.out.println("data "+data);

                if (player != null) {
                    later(player);
                } else {
                    Main.me.cache.put(nickname, name);
                }

//                System.out.println("cache "+Main.me.cache);

                OfflinePlayer offlinePlayer = player != null ? player : Main.getOfflinePlayer(nickname);

//                System.out.println("offlineplayer "+offlinePlayer);

                if (offlinePlayer != null) {
                    if (data.containsKey("vault")) {
//                        System.out.println("vault "+data.get("vault"));
                        try {
                            Main.me.giveVault(offlinePlayer,
                                    ((Number) data.get("vault")).doubleValue());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (data.containsKey("commands")) {
//                        System.out.println("commands "+data.get("commands"));
                        try {
                            for (String c : new ArrayList<>((List<String>) data.get("commands"))) {
                                if (c.startsWith("/")) c = c.substring(1);
                                getServer().dispatchCommand(getServer().getConsoleSender(),PlaceholderAPI.setPlaceholders(offlinePlayer, c));
//                                System.out.println("commands c "+c);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.runTask(Main.me);
    }

    public void later(Player player) {
        if (data.containsKey("item")) {
//            System.out.println("item "+data.get("item"));
            try {
                String[] ss = ((String)data.get("item")).split(" ");
                ItemStack item = new ItemStack(Material.valueOf(ss[0].toUpperCase()), ss.length == 1 ? 1 : Integer.parseInt(ss[1]));
                player.getInventory().addItem(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (data.containsKey("message")) {
//            System.out.println("message "+data.get("message"));
            try {
//                System.out.println("basecomponents "+Main.me.formatMessage(player, (String) data.get("message")));
                player.spigot().sendMessage(Main.me.formatMessage(player, (String) data.get("message")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (data.containsKey("as_player")) {
//            System.out.println("as_player "+data.get("as_player"));
            try {
                for (String c : new ArrayList<>((List<String>) data.get("as_player"))) {
//                    System.out.println("as_player c "+c);
                    player.chat(c);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
