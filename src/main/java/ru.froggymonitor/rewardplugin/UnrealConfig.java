package themixray.mainplugin.util;

import com.google.common.base.Charsets;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UnrealConfig extends HashMap<String, Object> {
    private static final Yaml yaml = new Yaml();

    private File file;

    public static UnrealConfig getByFileOrDefault(File file, Map<String,Object> def) {
        return new UnrealConfig(file,def);
    }

    public static UnrealConfig getByFileOrDefault(File file, Runnable on_def) {
        return new UnrealConfig(file,on_def);
    }

    public static UnrealConfig getByResource(JavaPlugin plugin, String resource) {
        return new UnrealConfig(plugin,resource);
    }

    public static UnrealConfig getByFile(JavaPlugin plugin, String file) {
        return new UnrealConfig(plugin, file);
    }

    public static UnrealConfig getByFile(File file) {
        return new UnrealConfig(file,new HashMap<>());
    }

    private UnrealConfig(File file, Map<String,Object> def) {
        this.file = file;
        if (file.exists()) {
            reload();
        } else {
            file.mkdirs();
            putAll(def);
            save();
        }
    }

    private UnrealConfig(File file, Runnable on_def) {
        this.file = file;
        if (!file.exists()) on_def.run();
        reload();
    }

    private UnrealConfig(JavaPlugin plugin, String filename) {
        file = Paths.get(plugin.getDataFolder().getPath(),filename).toFile();
        if (!file.exists()) plugin.saveResource(filename,false);
        reload();
    }

    public void reload() {
        try {
            clear();
            putAll(yaml.load(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void save() {
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
