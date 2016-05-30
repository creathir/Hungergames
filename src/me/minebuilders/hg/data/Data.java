package me.minebuilders.hg.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import me.minebuilders.hg.Bound;
import me.minebuilders.hg.Game;
import me.minebuilders.hg.HG;
import me.minebuilders.hg.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Data {
    private FileConfiguration arenadat = null;
    private File customConfigFile = null;
    private final HG plugin;

    public Data(HG plugin) {
        this.plugin = plugin;
        this.reloadCustomConfig();
        this.load();
    }

    public FileConfiguration getConfig() {
        return this.arenadat;
    }

    public void reloadCustomConfig() {
        if (this.customConfigFile == null) {
            this.customConfigFile = new File(this.plugin.getDataFolder(), "arenas.yml");
        }
        this.arenadat = YamlConfiguration.loadConfiguration((File)this.customConfigFile);
        InputStreamReader defConfigStream = null;
        try {
            defConfigStream = new InputStreamReader(this.plugin.getResource("arenas.yml"), "UTF8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration((Reader)defConfigStream);
            this.arenadat.setDefaults((Configuration)defConfig);
            this.arenadat.options().copyDefaults(true);
        }
    }

    public FileConfiguration getCustomConfig() {
        if (this.arenadat == null) {
            this.reloadCustomConfig();
        }
        return this.arenadat;
    }

    public void saveCustomConfig() {
        if (this.arenadat == null || this.customConfigFile == null) {
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
        int freeroam = this.plugin.getConfig().getInt("settings.free-roam");
        if (new File(this.plugin.getDataFolder(), "arenas.yml").exists()) {
            new me.minebuilders.hg.tasks.CompassTask(this.plugin);
            for (String s : this.arenadat.getConfigurationSection("arenas").getKeys(false)) {
                boolean isReady = true;
                ArrayList<Location> spawns = new ArrayList<Location>();
                Sign lobbysign = null;
                int timer = 0;
                int minplayers = 0;
                int maxplayers = 0;
                Bound b = null;
                try {
                    timer = this.arenadat.getInt("arenas." + s + ".info." + "timer");
                    minplayers = this.arenadat.getInt("arenas." + s + ".info." + "min-players");
                    maxplayers = this.arenadat.getInt("arenas." + s + ".info." + "max-players");
                }
                catch (Exception e) {
                    Util.warning("Unable to load infomation for arena " + s + "!");
                    isReady = false;
                }
                try {
                    lobbysign = (Sign)this.getSLoc(this.arenadat.getString("arenas." + s + "." + "lobbysign")).getBlock().getState();
                }
                catch (Exception e) {
                    Util.warning("Unable to load lobbysign for arena " + s + "!");
                    isReady = false;
                }
                try {
                    for (String l : this.arenadat.getStringList("arenas." + s + "." + "spawns")) {
                        spawns.add(this.getLocFromString(l));
                    }
                }
                catch (Exception e) {
                    Util.warning("Unable to load random spawns for arena " + s + "!");
                    isReady = false;
                }
                try {
                    b = new Bound(this.arenadat.getString("arenas." + s + ".bound." + "world"), this.BC(s, "x"), this.BC(s, "y"), this.BC(s, "z"), this.BC(s, "x2"), this.BC(s, "y2"), this.BC(s, "z2"));
                }
                catch (Exception e) {
                    Util.warning("Unable to load region bounds for arena " + s + "!");
                    isReady = false;
                }
                this.plugin.games.add(new Game(s, b, spawns, lobbysign, timer, minplayers, maxplayers, freeroam, isReady));
            }
        }
    }

    public int BC(String s, String st) {
        return this.arenadat.getInt("arenas." + s + ".bound." + st);
    }

    public Location getLocFromString(String s) {
        String[] h = s.split(":");
        return new Location(Bukkit.getServer().getWorld(h[0]), (double)Integer.parseInt(h[1]) + 0.5, (double)Integer.parseInt(h[2]), (double)Integer.parseInt(h[3]) + 0.5, Float.parseFloat(h[4]), Float.parseFloat(h[5]));
    }

    public Location getSLoc(String s) {
        String[] h = s.split(":");
        return new Location(Bukkit.getServer().getWorld(h[0]), (double)Integer.parseInt(h[1]), (double)Integer.parseInt(h[2]), (double)Integer.parseInt(h[3]));
    }
}

