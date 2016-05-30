package me.minebuilders.hg.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import me.minebuilders.hg.HG;
import me.minebuilders.hg.Util;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class RandomItems {
    private FileConfiguration item = null;
    private File customConfigFile = null;
    public int size = 0;
    private final HG plugin;

    public RandomItems(HG plugin) {
        this.plugin = plugin;
        this.reloadCustomConfig();
        this.load();
    }

    public void reloadCustomConfig() {
        if (this.customConfigFile == null) {
            this.customConfigFile = new File(this.plugin.getDataFolder(), "items.yml");
        }
        this.item = YamlConfiguration.loadConfiguration((File)this.customConfigFile);
        InputStreamReader defConfigStream = null;
        try {
            defConfigStream = new InputStreamReader(this.plugin.getResource("items.yml"), "UTF8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration((Reader)defConfigStream);
            this.item.setDefaults((Configuration)defConfig);
            this.item.options().copyDefaults(true);
        }
    }

    public FileConfiguration getCustomConfig() {
        if (this.item == null) {
            this.reloadCustomConfig();
        }
        return this.item;
    }

    public void saveCustomConfig() {
        if (this.item == null || this.customConfigFile == null) {
            return;
        }
        try {
            this.getCustomConfig().save(this.customConfigFile);
        }
        catch (IOException ex) {
            Util.log("Could not save config to " + this.customConfigFile);
        }
    }

    public void load() {
        this.size = 0;
        if (this.item.getStringList("items").isEmpty()) {
            this.setDefaultss();
            this.saveCustomConfig();
            this.reloadCustomConfig();
            Util.log("generating defaults for random items!");
        }
        for (String s : this.item.getStringList("items")) {            
            String[] arrstring = s.split(" ");
            int n = arrstring.length;
            int n2 = 0;
            while (n2 < n) {
                String p = arrstring[n2];
                if (p.startsWith("x:")) {
                    int c = Integer.parseInt(p.replace("x:", ""));
                    while (c != 0) {
                        --c;
                        this.plugin.items.put(this.plugin.items.size() + 1, this.plugin.ism.getItem(s.replace("x:", ""), true));
                        ++this.size;
                    }
                } else {
                    this.plugin.items.put(this.plugin.items.size() + 1, this.plugin.ism.getItem(s, true));
                }
                ++n2;
            }
            ++this.size;
        }
        Util.log(String.valueOf(this.plugin.items.size()) + " Random items have been loaded!");
    }

    public void setDefaultss() {
        ArrayList<String> s = new ArrayList<String>();
        s.add("272 1 x:5");
        s.add("283 1");
        s.add("282 1 x:2");
        s.add("291 1");
        s.add("298 1 x:2");
        s.add("299 1 x:2");
        s.add("300 1 x:2");
        s.add("306 1 x:2");
        s.add("307 1 x:2");
        s.add("308 1 x:2");
        s.add("309 1 x:2");
        s.add("261 1 x:3");
        s.add("262 20 x:2");
        s.add("335 1 x:2");
        s.add("346 1");
        s.add("345 1");
        s.add("280 1 name:&6TrackingStick_&aUses:_5");
        s.add("314 1");
        s.add("315 1");
        s.add("352 1 x:2");
        s.add("316 1");
        s.add("317 1");
        s.add("276 1 name:&6Death_Dealer");
        s.add("322 1");
        s.add("303 1 x:1");
        s.add("304 1 x:1");
        s.add("357 2 x:3");
        s.add("360 1 x:4");
        s.add("364 1 x:2");
        s.add("368 1 x:2");
        s.add("373:8194 1 x:2");
        s.add("373:8197 1 x:2");
        s.add("373:16420 1");
        s.add("373:16385 1 x:2");
        s.add("260 2 x:5");
        this.item.set("items", s);
    }
}

