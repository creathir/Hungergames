package me.minebuilders.hg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import me.minebuilders.hg.Game;
import me.minebuilders.hg.PlayerData;
import me.minebuilders.hg.PlayerSession;
import me.minebuilders.hg.Util;
import me.minebuilders.hg.commands.AddSpawnCmd;
import me.minebuilders.hg.commands.BaseCmd;
import me.minebuilders.hg.commands.CreateCmd;
import me.minebuilders.hg.commands.DebugCmd;
import me.minebuilders.hg.commands.DeleteCmd;
import me.minebuilders.hg.commands.JoinCmd;
import me.minebuilders.hg.commands.KitCmd;
import me.minebuilders.hg.commands.LeaveCmd;
import me.minebuilders.hg.commands.ListCmd;
import me.minebuilders.hg.commands.ListGamesCmd;
import me.minebuilders.hg.commands.ReloadCmd;
import me.minebuilders.hg.commands.SetExitCmd;
import me.minebuilders.hg.commands.SetLobbyWallCmd;
import me.minebuilders.hg.commands.StartCmd;
import me.minebuilders.hg.commands.StopCmd;
import me.minebuilders.hg.commands.TeamCmd;
import me.minebuilders.hg.commands.ToggleCmd;
import me.minebuilders.hg.commands.WandCmd;
import me.minebuilders.hg.data.Data;
import me.minebuilders.hg.data.RandomItems;
import me.minebuilders.hg.listeners.CancelListener;
import me.minebuilders.hg.listeners.CommandListener;
import me.minebuilders.hg.listeners.GameListener;
import me.minebuilders.hg.listeners.WandListener;
import me.minebuilders.hg.managers.ItemStackManager;
import me.minebuilders.hg.managers.KillManager;
import me.minebuilders.hg.managers.KitManager;
import me.minebuilders.hg.managers.Manager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class HG
extends JavaPlugin {
    public HashMap<String, BaseCmd> cmds = new HashMap<String, BaseCmd>();
    public HashMap<UUID, PlayerData> players = new HashMap<UUID, PlayerData>();
    public HashMap<String, PlayerSession> playerses = new HashMap<String, PlayerSession>();
    public HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
    public List<Game> games = new ArrayList<Game>();
    public static HG plugin;
    public static Manager manager;
    public static Data arenaconfig;
    public static KillManager killmanager;
    public static RandomItems ri;
    public KitManager kit;
    public ItemStackManager ism;

    public void onEnable() {
        new me.minebuilders.hg.Config(this);
        plugin = this;
        arenaconfig = new Data(this);
        killmanager = new KillManager();
        this.kit = new KitManager();
        this.ism = new ItemStackManager(this);
        ri = new RandomItems(this);
        manager = new Manager(this);
        this.getCommand("hg").setExecutor((CommandExecutor)new CommandListener(this));
        this.getServer().getPluginManager().registerEvents((Listener)new WandListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new CancelListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new GameListener(this), (Plugin)this);
        this.loadCmds();
        Util.log("Hugergames has been enabled!");
    }

    public void onDisable() {
        this.stopAll();
        plugin = null;
        manager = null;
        arenaconfig = null;
        killmanager = null;
        this.kit = null;
        this.ism = null;
        ri = null;
        Util.log("Hugergames has been disabled!");
    }

    public void loadCmds() {
        this.cmds.put("team", new TeamCmd());
        this.cmds.put("addspawn", new AddSpawnCmd());
        this.cmds.put("create", new CreateCmd());
        this.cmds.put("join", new JoinCmd());
        this.cmds.put("leave", new LeaveCmd());
        this.cmds.put("reload", new ReloadCmd());
        this.cmds.put("setlobbywall", new SetLobbyWallCmd());
        this.cmds.put("wand", new WandCmd());
        this.cmds.put("kit", new KitCmd());
        this.cmds.put("debug", new DebugCmd());
        this.cmds.put("list", new ListCmd());
        this.cmds.put("listgames", new ListGamesCmd());
        this.cmds.put("forcestart", new StartCmd());
        this.cmds.put("stop", new StopCmd());
        this.cmds.put("toggle", new ToggleCmd());
        this.cmds.put("setexit", new SetExitCmd());
        this.cmds.put("delete", new DeleteCmd());
        for (String bc : this.cmds.keySet()) {
            this.getServer().getPluginManager().addPermission(new Permission("hg." + bc));
        }
    }

    public void stopAll() {
        ArrayList<UUID> ps = new ArrayList<UUID>();
        for (Game g : this.games) {
            g.cancelTasks();
            g.forceRollback();
            ps.addAll(g.getPlayers());
        }
        for (UUID s : ps) {
            Player p = Bukkit.getPlayer((UUID)s);
            if (p == null) continue;
            this.players.get(s).getGame().leave(p);
        }
        this.players.clear();
        this.games.clear();
    }
}

