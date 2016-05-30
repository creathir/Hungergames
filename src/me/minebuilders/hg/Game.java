package me.minebuilders.hg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.minebuilders.hg.Bound;
import me.minebuilders.hg.Config;
import me.minebuilders.hg.HG;
import me.minebuilders.hg.PlayerData;
import me.minebuilders.hg.SBDisplay;
import me.minebuilders.hg.Status;
import me.minebuilders.hg.Team;
import me.minebuilders.hg.Util;
import me.minebuilders.hg.Vault;
import me.minebuilders.hg.mobhandler.Spawner;
import me.minebuilders.hg.tasks.ChestDropTask;
import me.minebuilders.hg.tasks.FreeRoamTask;
import me.minebuilders.hg.tasks.StartingTask;
import me.minebuilders.hg.tasks.TimerTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Game {
    private String name;
    private List<Location> spawns;
    private Bound b;
    private List<UUID> players = new ArrayList<UUID>();
    private ArrayList<Location> chests = new ArrayList<Location>();
    private List<BlockState> blocks = new ArrayList<BlockState>();
    private Location exit;
    private Status status;
    private int minplayers;
    private int maxplayers;
    private int time;
    private Sign s;
    private Sign s1;
    private Sign s2;
    private int roamtime;
    private SBDisplay sb;
    private Spawner spawner;
    private FreeRoamTask freeroam;
    private StartingTask starting;
    private TimerTask timer;
    private ChestDropTask chestdrop;

    public Game(String s, Bound bo, List<Location> spawns, Sign lobbysign, int timer, int minplayers, int maxplayers, int roam, boolean isready) {
        this.name = s;
        this.b = bo;
        this.spawns = spawns;
        this.s = lobbysign;
        this.time = timer;
        this.minplayers = minplayers;
        this.maxplayers = maxplayers;
        this.roamtime = roam;
        this.status = isready ? Status.STOPPED : Status.BROKEN;
        this.setChests();
        this.setLobbyBlock(lobbysign);
        this.sb = new SBDisplay(this);
    }

    public Game(String s, Bound c, int timer, int minplayers, int maxplayers, int roam) {
        this.name = s;
        this.time = timer;
        this.minplayers = minplayers;
        this.maxplayers = maxplayers;
        this.roamtime = roam;
        this.spawns = new ArrayList<Location>();
        this.b = c;
        this.status = Status.NOTREADY;
        this.setChests();
        this.sb = new SBDisplay(this);
    }

    public Bound getRegion() {
        return this.b;
    }

    public void forceRollback() {
        Collections.reverse(this.blocks);
        for (BlockState st : this.blocks) {
            st.update(true);
        }
    }

    public void setStatus(Status st) {
        this.status = st;
        this.updateLobbyBlock();
    }

    public void addState(BlockState s) {
        if (s.getType() != Material.AIR) {
            this.blocks.add(s);
        }
    }

    public void recordBlockBreak(Block bl) {
        Block top = bl.getRelative(BlockFace.UP);
        if (!top.getType().isSolid() || !top.getType().isBlock()) {
            this.addState(bl.getRelative(BlockFace.UP).getState());
        }
        BlockFace[] arrblockFace = Util.faces;
        int n = arrblockFace.length;
        int n2 = 0;
        while (n2 < n) {
            BlockFace bf = arrblockFace[n2];
            Block rel = bl.getRelative(bf);
            if (Util.isAttached(bl, rel)) {
                this.addState(rel.getState());
            }
            ++n2;
        }
        this.addState(bl.getState());
    }

    public void recordBlockPlace(BlockState bs) {
        this.blocks.add(bs);
    }

    public Status getStatus() {
        return this.status;
    }

    public List<BlockState> getBlocks() {
        Collections.reverse(this.blocks);
        return this.blocks;
    }

    public void resetBlocks() {
        this.blocks.clear();
    }

    public void setChests() {
        this.chests.clear();
        for (Location bl : this.b.getBlocks(Material.CHEST)) {
            this.chests.add(bl);
        }
    }

    public void msgAllMulti(String[] sta) {
        String[] arrstring = sta;
        int n = arrstring.length;
        int n2 = 0;
        while (n2 < n) {
            String s = arrstring[n2];
            for (UUID st : this.players) {
                Player p = Bukkit.getPlayer((UUID)st);
                if (p == null) continue;
                Util.msg((CommandSender)p, s);
            }
            ++n2;
        }
    }

    public List<UUID> getPlayers() {
        return this.players;
    }

    public List<String> getPlayerNames() {
        ArrayList<String> playerNames = new ArrayList<String>();
        for (UUID st : this.players) {
            Player p = Bukkit.getPlayer((UUID)st);
            if (p == null) continue;
            playerNames.add(p.getName());
        }
        return playerNames;
    }

    public String getName() {
        return this.name;
    }

    public boolean isInRegion(Location l) {
        return this.b.isInRegion(l);
    }

    public List<Location> getSpawns() {
        return this.spawns;
    }

    public int getRoamTime() {
        return this.roamtime;
    }

    public void join(Player p) {
        if (this.status != Status.WAITING && this.status != Status.STOPPED && this.status != Status.COUNTDOWN) {
            p.sendMessage((Object)ChatColor.RED + "This arena is not ready! Please wait before joining!");
        } else if (this.maxplayers <= this.players.size()) {
            p.sendMessage((Object)ChatColor.RED + this.name + " is currently full!");
        } else {
            if (p.isInsideVehicle()) {
                p.leaveVehicle();
            }
            this.players.add(p.getUniqueId());
            HG.plugin.players.put(p.getUniqueId(), new PlayerData(p, this));
            p.teleport(this.pickSpawn());
            this.heal(p);
            this.freeze(p);
            if (this.players.size() >= this.minplayers && this.status.equals((Object)Status.WAITING)) {
                this.startPreGame();
            } else if (this.status == Status.WAITING) {
                this.msgDef("&4(&3" + p.getName() + "&b Has joined the game" + (this.minplayers - this.players.size() <= 0 ? "!" : new StringBuilder(": ").append(this.minplayers - this.players.size()).append(" players to start!").toString()) + "&4)");
            }
            this.kitHelp(p);
            if (this.players.size() == 1) {
                this.status = Status.WAITING;
            }
            this.updateLobbyBlock();
            this.sb.setSB(p);
            this.sb.setAlive();
        }
    }

    public void kitHelp(Player p) {
        String kit = HG.plugin.kit.getKitList();
        Util.scm((CommandSender)p, "&8     ");
        Util.scm((CommandSender)p, "&9&l>----------[&b&lWelcome to HungerGames&9&l]----------<");
        Util.scm((CommandSender)p, "&9&l - &bPick a kit using &c/hg kit <kit-name>");
        Util.scm((CommandSender)p, "&9&lKits:&b" + kit);
        Util.scm((CommandSender)p, "&9&l>------------------------------------------<");
    }

    public void respawnAll() {
        for (UUID st : this.players) {
            Player p = Bukkit.getPlayer((UUID)st);
            if (p == null) continue;
            p.teleport(this.pickSpawn());
        }
    }

    public void startPreGame() {
        this.setStatus(Status.COUNTDOWN);
        this.starting = new StartingTask(this);
        this.updateLobbyBlock();
    }

    public void startFreeRoam() {
        this.status = Status.BEGINNING;
        HG.manager.restoreChests(this);
        this.b.removeEntities();
        this.freeroam = new FreeRoamTask(this);
    }

    public void startGame() {
        this.status = Status.RUNNING;
        if (Config.spawnmobs) {
            this.spawner = new Spawner(this, Config.spawnmobsinterval);
        }
        if (Config.randomChest) {
            this.chestdrop = new ChestDropTask(this);
        }
        this.timer = new TimerTask(this, this.time);
        this.updateLobbyBlock();
    }

    public void addSpawn(Location l) {
        this.spawns.add(l);
    }

    public Location pickSpawn() {
        int spawn = this.players.size() - 1;
        if (this.containsPlayer(this.spawns.get(spawn))) {
            for (Location l : this.spawns) {
                if (this.containsPlayer(l)) continue;
                return l;
            }
        }
        return this.spawns.get(spawn);
    }

    public boolean containsPlayer(Location l) {
        if (l == null) {
            return false;
        }
        for (UUID s : this.players) {
            Player p = Bukkit.getPlayer((UUID)s);
            if (p == null || !p.getLocation().getBlock().equals((Object)l.getBlock())) continue;
            return true;
        }
        return false;
    }

    public void msgAll(String s) {
        for (UUID st : this.players) {
            Player p = Bukkit.getPlayer((UUID)st);
            if (p == null) continue;
            Util.msg((CommandSender)p, s);
        }
    }

    public void msgDef(String s) {
        for (UUID st : this.players) {
            Player p = Bukkit.getPlayer((UUID)st);
            if (p == null) continue;
            Util.scm((CommandSender)p, s);
        }
    }

    public void updateLobbyBlock() {
        this.s1.setLine(1, this.status.getName());
        this.s2.setLine(1, (Object)ChatColor.BOLD + "" + this.players.size() + "/" + this.maxplayers);
        this.s1.update(true);
        this.s2.update(true);
    }

    public void heal(Player p) {
        for (PotionEffect ef : p.getActivePotionEffects()) {
            p.removePotionEffect(ef.getType());
        }
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setFireTicks(0);
    }

    public void freeze(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 23423525, -10));
        p.setWalkSpeed(1.0E-4f);
        p.setFoodLevel(1);
        p.setAllowFlight(false);
        p.setFlying(false);
        p.setGameMode(GameMode.SURVIVAL);
    }

    public void unFreeze(Player p) {
        p.removePotionEffect(PotionEffectType.JUMP);
        p.setWalkSpeed(0.2f);
    }

    public boolean setLobbyBlock(Sign sign) {
        try {
            this.s = sign;
            Block c = this.s.getBlock();
            @SuppressWarnings("deprecation")
			BlockFace face = Util.getSignFace(c.getData());
            this.s1 = (Sign)c.getRelative(face).getState();
            this.s2 = (Sign)this.s1.getBlock().getRelative(face).getState();
            this.s.setLine(0, (Object)ChatColor.DARK_BLUE + "" + (Object)ChatColor.BOLD + "HungerGames");
            this.s.setLine(1, (Object)ChatColor.BOLD + ""  + this.name);
            this.s.setLine(2, (Object)ChatColor.BOLD + ""  + "Click To Join");
            this.s1.setLine(0, (Object)ChatColor.DARK_BLUE + ""  + (Object)ChatColor.BOLD + "Game Status");
            this.s1.setLine(1, this.status.getName());
            this.s2.setLine(0, (Object)ChatColor.DARK_BLUE + ""  + (Object)ChatColor.BOLD + "Alive");
            this.s2.setLine(1, (Object)ChatColor.BOLD + ""  + 0 + "/" + this.maxplayers);
            this.s.update(true);
            this.s1.update(true);
            this.s2.update(true);
        }
        catch (Exception e) {
            return false;
        }
        try {
            String[] h = HG.plugin.getConfig().getString("settings.globalexit").split(":");
            this.exit = new Location(Bukkit.getServer().getWorld(h[0]), (double)Integer.parseInt(h[1]) + 0.5, (double)Integer.parseInt(h[2]) + 0.1, (double)Integer.parseInt(h[3]) + 0.5, Float.parseFloat(h[4]), Float.parseFloat(h[5]));
        }
        catch (Exception e) {
            this.exit = this.s.getWorld().getSpawnLocation();
        }
        return true;
    }

    public void setExit(Location l) {
        this.exit = l;
    }

    public void cancelTasks() {
        if (this.spawner != null) {
            this.spawner.stop();
        }
        if (this.timer != null) {
            this.timer.stop();
        }
        if (this.starting != null) {
            this.starting.stop();
        }
        if (this.freeroam != null) {
            this.freeroam.stop();
        }
        if (this.chestdrop != null) {
            this.chestdrop.shutdown();
        }
    }

    public void stop() {
        ArrayList<UUID> win = new ArrayList<UUID>();
        this.cancelTasks();
        for (UUID s : this.players) {
            Player p = Bukkit.getPlayer((UUID)s);
            if (p == null) continue;
            this.heal(p);
            this.exit(p);
            HG.plugin.players.get(p.getUniqueId()).restore(p);
            HG.plugin.players.remove(p.getUniqueId());
            win.add(p.getUniqueId());
            this.sb.restoreSB(p);
        }
        this.players.clear();
        if (!win.isEmpty() && Config.giveReward) {
            double db = Config.cash / win.size();
            for (UUID s2 : win) {
                Player p = Bukkit.getPlayer((UUID)s2);
                if (p != null) {
                    Vault.economy.depositPlayer((OfflinePlayer)p, db);
                }
                Util.msg((CommandSender)p, "&aYou won " + db + " for winning HungerGames!");
            }
        }
        Util.broadcast("&l&3" + Util.translateStop(win) + " &l&bWon HungerGames at arena " + this.name + "!");
        if (!this.blocks.isEmpty()) {
            new me.minebuilders.hg.Rollback(this);
        } else {
            this.status = Status.STOPPED;
            this.updateLobbyBlock();
        }
        this.b.removeEntities();
        this.sb.resetAlive();
    }

    public void leave(Player p) {
        this.players.remove(p.getUniqueId());
        this.unFreeze(p);
        this.heal(p);
        this.exit(p);
        HG.plugin.players.get(p.getUniqueId()).restore(p);
        HG.plugin.players.remove(p.getUniqueId());
        if (this.status == Status.RUNNING || this.status == Status.BEGINNING) {
            if (this.isGameOver()) {
                this.stop();
            }
        } else if (this.status == Status.WAITING) {
            this.msgDef("&3&l" + p.getName() + "&l&c Has left the game" + (this.minplayers - this.players.size() <= 0 ? "!" : new StringBuilder(": ").append(this.minplayers - this.players.size()).append(" players to start!").toString()));
        }
        this.updateLobbyBlock();
        this.sb.restoreSB(p);
        this.sb.setAlive();
    }

    public boolean isGameOver() {
        if (this.players.size() <= 1) {
            return true;
        }
        for (Map.Entry<UUID, PlayerData> f : HG.plugin.players.entrySet()) {
            Team t = f.getValue().getTeam();
            if (t == null || t.getPlayers().size() < this.players.size()) continue;
            List<UUID> ps = t.getPlayers();
            for (UUID s : this.players) {
                if (ps.contains(s)) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    public void addChests(Location b) {
        this.chests.add(b);
    }

    public ArrayList<Location> getChests() {
        return this.chests;
    }

    public void exit(Player p) {
        Util.clearInv(p);
        if (this.exit == null) {
            p.teleport(this.s.getWorld().getSpawnLocation());
        } else {
            p.teleport(this.exit);
        }
    }

    public int getMaxPlayers() {
        return this.maxplayers;
    }

    public boolean isLobbyValid() {
        try {
            if (this.s instanceof Sign && this.s1 instanceof Sign && this.s2 instanceof Sign) {
                return true;
            }
        }
        catch (Exception e) {
            return false;
        }
        return false;
    }
}

