package themixray.monitoringreward;

import net.md_5.bungee.api.ChatColor;
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
    public Random rand;

    public FileConfiguration file;

    public Map<String,Map<String,Integer>> cache;
    public ConfigReader cache_conf;
    public FileConfiguration cache_file;

    public String hotmc_token;
    public String mineserv_token;
    public String minecraftrating_token;
    public String misterlauncher_token;

    public String site_ip;
    public int site_port;

    public Map<String,String> reward_messages;
    public Map<String,String> super_reward_messages;
    public int super_reward_votes;
    public int spec_super_reward_votes;

    public boolean player_offline_only_money;
    public boolean add_vote_command;
    public boolean add_general_monitoring;
    public boolean add_specific_monitoring;

    public List<Map<String,Object>> super_reward;
    public List<Map<String,Object>> spec_super_reward;
    public List<Map<String,Object>> default_reward;

    public Map<String,Map<String,Object>> reward_when_join;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        me = this;

        new ReloadCommand(this);

        rand = new Random();

        reward_when_join = new HashMap<>();

        saveDefaultConfig();
        file = getConfig();

        cache = new HashMap<>();

        File f = Path.of(getDataFolder().getPath(),"cache.yml").toFile();
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cache_conf = new ConfigReader(this,"./","cache.yml");
        cache_file = cache_conf.getConfig();

        Map<String,Object> players_sect = cache_file.getValues(false);
        for (Map.Entry<String,Object> p:players_sect.entrySet()) {
            Map<String,Object> monitoring_sect = ((MemorySection)p.getValue()).getValues(false);
            Map<String,Integer> monitoring = new HashMap<>();
            for (Map.Entry<String,Object> m:monitoring_sect.entrySet())
                monitoring.put(m.getKey(),(Integer)m.getValue());
            cache.put(p.getKey(),monitoring);
        }

        site_ip = file.getString("site_ip");
        site_port = file.getInt("site_port");

        hotmc_token = file.getString("hotmc_token");
        minecraftrating_token = file.getString("minecraftrating_token");
        misterlauncher_token = file.getString("misterlauncher_token");
        mineserv_token = file.getString("mineserv_token");

        reward_messages = new HashMap<>();
        Map<String,Object> reward_message_sect = ((MemorySection)file.get("reward_message")).getValues(false);
        for (Map.Entry<String,Object> e:reward_message_sect.entrySet())
            reward_messages.put(e.getKey(),translateHexCodes((String) e.getValue()));

        super_reward_messages = new HashMap<>();
        Map<String,Object> super_reward_message_sect = ((MemorySection)file.get("super_reward_message")).getValues(false);
        for (Map.Entry<String,Object> e:super_reward_message_sect.entrySet())
            super_reward_messages.put(e.getKey(),translateHexCodes((String) e.getValue()));

        player_offline_only_money = file.getBoolean("player_offline_only_money");
        add_vote_command = file.getBoolean("add_vote_command");
        add_general_monitoring = file.getBoolean("add_general_monitoring");
        add_specific_monitoring = file.getBoolean("add_specific_monitoring");
        super_reward_votes = file.getInt("super_reward_votes");
        super_reward = new ArrayList<>();
        List<Map<?, ?>> super_reward_sect = file.getMapList("super_reward");
        for (Map<?, ?> e:super_reward_sect) {
            Map<String, Object> i = (Map<String, Object>) e;
            Map<String, Object> o = new HashMap<>();

            String type = (String) i.get("type");
            if (type.equals("item")) {
                o.put("name", i.get("name"));
                o.put("item", ItemsParser.parseItem((Map<String, Object>) i.get("item")));
            } else if (type.equals("money")) {
                o.put("count", i.get("count"));
            }
            o.put("type", i.get("type"));
            super_reward.add(o);
        }
        spec_super_reward_votes = file.getInt("special_super_reward_votes");
        spec_super_reward = new ArrayList<>();
        List<Map<?, ?>> spec_super_reward_sect = file.getMapList("special_super_reward");
        for (Map<?, ?> e:spec_super_reward_sect) {
            Map<String, Object> i = (Map<String, Object>) e;
            Map<String, Object> o = new HashMap<>();

            String type = (String) i.get("type");
            if (type.equals("item")) {
                o.put("name", i.get("name"));
                o.put("item", ItemsParser.parseItem((Map<String, Object>) i.get("item")));
            } else if (type.equals("money")) {
                o.put("count", i.get("count"));
            }
            o.put("type", i.get("type"));
            spec_super_reward.add(o);
        }
        default_reward = new ArrayList<>();
        List<Map<?, ?>> default_reward_sect = file.getMapList("default_reward");
        for (Map<?, ?> e:default_reward_sect) {
            Map<String, Object> i = (Map<String, Object>) e;
            Map<String, Object> o = new HashMap<>();

            String type = (String) i.get("type");
            if (type.equals("item")) {
                o.put("name", i.get("name"));
                o.put("item", ItemsParser.parseItem((Map<String, Object>) i.get("item")));
            } else if (type.equals("money")) {
                o.put("count", i.get("count"));
            }
            o.put("type", i.get("type"));
            default_reward.add(o);
        }

        site = new SitePart(site_ip,site_port);
        getLogger().info("Server "+site_ip+":"+site_port+" started!");

        getServer().getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        site.server.stop(0);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String player = e.getPlayer().getName();
        if (reward_when_join.containsKey(player)) {
            Map<String,Object> r = reward_when_join.get(player);
            String type = (String) r.get("type");
            if (type.equals("item")) {
                ItemStack item = (ItemStack) r.get("item");
                String name = (String) r.get("name");
                p.getInventory().addItem(item);
                String rm = (String) r.get("message");
                if (rm.contains("%d")) {
                    p.sendMessage(String.format(rm,name,
                        getVotesBeforeSuper(player)));
                } else {
                    p.sendMessage(String.format(rm,name));
                }
            }
            reward_when_join.remove(player);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (add_vote_command) {
            String cmd = e.getMessage().split(" ")[0];
            if (cmd.startsWith("/")) cmd = cmd.substring(1);

            if (cmd.startsWith("vote")) {
                String monitoring = cmd.split("/")[1];
                sendVote(e.getPlayer().getName(), monitoring);
                e.setCancelled(true);
            }
        }
    }

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

    public void sendVote(String player, String monitoring) {
        getLogger().info(player+" voted on "+monitoring);
        if (add_general_monitoring) addVoteGeneral(player,monitoring);
        if (add_specific_monitoring) addVote(player,monitoring);
    }

    public void addVote(String player, String monitoring) {
        if (cache.containsKey(player)) {
            cache.get(player).put(monitoring,
                cache.get(player).getOrDefault(
                monitoring,0)+1);
        } else {
            Map<String,Integer> m = new HashMap<>();
            m.put(monitoring,1);
            cache.put(player,m);
        }
        if (cache.get(player).get(monitoring) >= spec_super_reward_votes) {
            giveReward(player, spec_super_reward, super_reward_messages.get(monitoring));
            cache.get(player).put(monitoring, 0);
        }
        saveCache();
    }

    public void addVoteGeneral(String player, String monitoring) {
        if (cache.containsKey(player)) {
            cache.get(player).put("general",
                    cache.get(player).getOrDefault(
                            "general",0)+1);
        } else {
            Map<String,Integer> m = new HashMap<>();
            m.put("general",1);
            cache.put(player,m);
        }
        if (cache.get(player).get("general") >= super_reward_votes) {
            giveReward(player, super_reward, super_reward_messages.get("general"));
            cache.get(player).put("general", 0);
        } else {
            giveReward(player, default_reward, reward_messages.get(monitoring));
        }
        saveCache();
    }

    public int getVotesBeforeSuper(String player) {
        return super_reward_votes-cache.get(player).getOrDefault("general",0);
    }

    public void giveReward(String player, List<Map<String,Object>> rewards, String message) {
        Player p = Bukkit.getPlayer(player);
        if (player_offline_only_money && p == null) {
            while (true) {
                int index = rand.nextInt(0, rewards.size());
                Map<String, Object> r = rewards.get(index);
                String type = (String) r.get("type");
                if (!type.equals("money")) continue;
                econ.depositPlayer(getOfflinePlayer(player), (int) r.get("count"));
                break;
            }
        } else {
            int index = rand.nextInt(0, rewards.size());
            Map<String, Object> r = rewards.get(index);
            String type = (String) r.get("type");
            String name = null;
            if (type.equals("item")) {
                ItemStack item = (ItemStack) r.get("item");
                name = (String) r.get("name");
                if (p != null) p.getInventory().addItem(item);
                else {
                    Map<String, Object> t = new HashMap<>(r);
                    t.put("message", message);
                    reward_when_join.put(player,t);
                }
            } else if (type.equals("money")) {
                int count = (int) r.get("count");
                econ.depositPlayer(getOfflinePlayer(player),count);
                name = econ.format(count);
            }
            if (p != null) {
                if (message.contains("%d")) {
                    p.sendMessage(String.format(message,name,
                            getVotesBeforeSuper(player)));
                } else {
                    p.sendMessage(String.format(message,name));
                }
            }
        }
    }

    public OfflinePlayer getOfflinePlayer(String name) {
        for (OfflinePlayer p:Bukkit.getOfflinePlayers()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public void saveCache() {
        for (Map.Entry<String,Map<String,Integer>> e:cache.entrySet())
            cache_file.set(e.getKey(),e.getValue());
        cache_conf.saveConfig();
    }

    public static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");

    public String translateHexCodes(String textToTranslate) {
        Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }
}
