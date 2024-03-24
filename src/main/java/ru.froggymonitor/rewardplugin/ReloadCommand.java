package ru.froggymonitor.rewardplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {
    public PluginCommand pluginCommand;

    public ReloadCommand() {
        pluginCommand = Main.me.getCommand("reload");
        pluginCommand.setTabCompleter(this);
        pluginCommand.setExecutor(this);
    }

    public List<String> onTabComplete(CommandSender sender,
                                      Command command,
                                      String alias,
                                      String[] args) {
        return new ArrayList<>();
    }

    public boolean onCommand(CommandSender sender,
                             Command command,
                             String alias,
                             String[] args) {
        try {
            Main.me.onDisable();
            Main.me.onEnable();
            for (Player p : Bukkit.getOnlinePlayers())
                Main.me.onJoin(new PlayerJoinEvent(p, ""));

            sender.sendMessage("Перезагрузка успешно завершена");
            return true;
        } catch (Exception e) {
            StringWriter buffer = new StringWriter();
            PrintWriter writer = new PrintWriter(buffer);
            e.printStackTrace(writer);
            sender.sendMessage(buffer.toString());
        }
        sender.sendMessage("При выполнении команды возникла неизвестная ошибка");
        return true;
    }
}
