package ru.froggymonitor.rewardplugin;

import com.google.common.base.Charsets;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UnrealConfig extends HashMap<String, Object> {
    private Yaml yaml;
    private File file;

    public UnrealConfig(JavaPlugin plugin, String filename) {
        file = Paths.get(plugin.getDataFolder().getPath(),filename).toFile();
        if (!file.exists()) plugin.saveResource(filename,false);
        yaml = new Yaml();
        reloadConfig();
    }

    public File getFile() {
        return file;
    }

    public void reloadConfig() {
        try {
            clear();
            putAll(yaml.load(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            yaml.dump(this,new FileWriter(file, Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String,Object> clone() {
        return new HashMap<>(this);
    }
}
