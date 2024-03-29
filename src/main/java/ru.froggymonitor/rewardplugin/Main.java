package ru.froggymonitor.rewardplugin;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.data.type.TripwireHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.*;

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

    public String external_host;

    public boolean enable_logs;
    public MessageFormatting message_formatting;

    public Reward vote_reward;
    public Reward add_comment_reward;
    public Reward del_comment_reward;

    public String vote_page;
    public String comment_page;

    public Map<String, String> cache;

    public File cache_file;

    public boolean has_placeholderapi;
    public boolean has_vault;

    public String bind_host;
    public int bind_port;

    public ReloadCommand reload_command;

    @Override
    public void onEnable() {
        has_vault = setupEconomy();
        has_placeholderapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        me = this;

        reload_command = new ReloadCommand();

        conf = new UnrealConfig(this, "config.yml");

        vote_page = "/api/vote";
        comment_page = "/api/comment";

        secret_token = (String) conf.get("secret_token");
        external_host = (String) conf.get("external_host");

        enable_logs = (Boolean) conf.get("enable_logs");
        message_formatting = MessageFormatting.getFormatting((String) conf.get("message_formatting"));

        vote_reward = new Reward("vote", (Map<String, Object>) conf.get("vote"));
        add_comment_reward = new Reward("add_comment", (Map<String, Object>) conf.get("add_comment"));
        del_comment_reward = new Reward("del_comment", (Map<String, Object>) conf.get("del_comment"));

        bind_host = (String) conf.get("bind_host");
        bind_port = ((Number) conf.get("bind_port")).intValue();

        httpClient = HttpClient.newHttpClient();

        sendRewardUrls();

        cache_file = new File(getDataFolder().getPath(), "cache");

        loadCache();

        site = new SitePart(bind_host,bind_port,0);
        site.start();

        getServer().getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        tryCatchIgnore(() -> {
            saveCache();
            site.stop();
        });
    }

    public interface tryCatchRunnable {
        public void run() throws Throwable;
    }

    public void tryCatchIgnore(tryCatchRunnable r) {
        try {
            r.run();
        } catch (Throwable ignored) {}
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String n = p.getName();

        if (cache.containsKey(n)) {
            switch (cache.get(n)) {
                case ("vote") -> vote_reward.later(p);
                case ("add_comment") -> add_comment_reward.later(p);
                case ("del_comment") -> del_comment_reward.later(p);
            }

            Main.me.cache.remove(n);
        }
    }

    public void saveCache() {
        if (cache.isEmpty())  {
            cache_file.delete();
        } else {
            try {
                if (!cache_file.exists())
                    cache_file.createNewFile();

                StringBuilder text = new StringBuilder();
                for (Map.Entry<String, String> e : cache.entrySet())
                    text.append(e.getKey()).append("=").append(e.getValue()).append("\n");

                Files.writeString(cache_file.toPath(), text.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void loadCache() {
        cache = new HashMap<>();

        if (cache_file.exists()) {
            try {
                String text = Files.readString(cache_file.toPath());

                for (String s : text.split("\n")) {
                    String[] ss = s.split("=");
                    cache.put(ss[0], ss[1]);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        for (OfflinePlayer p:Bukkit.getOfflinePlayers()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public HttpClient httpClient;

    public void sendRewardUrls() {
        String start_url = "http://"+external_host+":"+bind_port;

        String body = "{\"secret_token\": \""+secret_token+"\", "+
                        "\"vote_url\": \""+start_url+vote_page+"\", "+
                        "\"comment_url\": \""+start_url+comment_page+"\"}";

        try {
            URL url = new URL("http://froggymonitor.ru/api/set_reward_urls");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(body.getBytes().length));

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(body);
            wr.close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BaseComponent[] formatMessage(Player player, String text) {
        return Main.me.message_formatting.format(has_placeholderapi ? PlaceholderAPI.setPlaceholders(player, text) : text);
    }

    public void giveVault(OfflinePlayer player, double amount) {
        if (has_vault) econ.depositPlayer(player, amount);
    }
}
