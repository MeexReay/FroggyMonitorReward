package ru.froggymonitor.rewardplugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin implements Listener {
    public static Main me;

    public SitePart site;
    public Economy econ;

    public UnrealConfig conf;

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public String secret_token;

    public String comment_page;
    public String vote_page;

    public Reward vote_reward;
    public Reward add_comment_reward;
    public Reward del_comment_reward;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("[FroggyMonitorReward] - Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().severe("[FroggyMonitorReward] - Disabled due to no PlaceholderAPI dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        me = this;

        conf = new UnrealConfig(this, "config.yml");

        secret_token = (String) conf.get("secret_token");

        comment_page = (String) conf.get("comment_page");
        vote_page = (String) conf.get("vote_page");

        vote_reward = new Reward((Map<String, Object>) conf.get("vote"));
        add_comment_reward = new Reward((Map<String, Object>) conf.get("add_comment"));
        del_comment_reward = new Reward((Map<String, Object>) conf.get("del_comment"));

        site = new SitePart(
                (String) conf.get("site_host"),
                ((Number) conf.get("site_port")).intValue(),
                ((Number) conf.get("site_backlog")).intValue());

        site.start();

        getServer().getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        site.stop();
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        for (OfflinePlayer p:Bukkit.getOfflinePlayers()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
}
