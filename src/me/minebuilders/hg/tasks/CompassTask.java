package me.minebuilders.hg.tasks;

import java.util.UUID;
import me.minebuilders.hg.Game;
import me.minebuilders.hg.HG;
import me.minebuilders.hg.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class CompassTask
implements Runnable {
    private HG plugin;

    public CompassTask(HG plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)HG.plugin, (Runnable)this, 25, 25);
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData pd;
            if (!p.getInventory().contains(Material.COMPASS) || (pd = this.plugin.players.get(p.getName())) == null) continue;
            String[] st = this.getNearestPlayer(p, pd);
            String info = ChatColor.WHITE + "" + ChatColor.BOLD + "Nearest Player: " + (Object)ChatColor.RED + st[0] + "    " + (Object)ChatColor.WHITE + (Object)ChatColor.BOLD + "Distance: " + (Object)ChatColor.RED + st[1];
            for (ItemStack it : p.getInventory()) {
                if (it == null || it.getType() != Material.COMPASS) continue;
                ItemMeta im = it.getItemMeta();
                im.setDisplayName(info);
                it.setItemMeta(im);
            }
        }
    }

    private int cal(int i) {
        if (i < 0) {
            return - i;
        }
        return i;
    }

    public String[] getNearestPlayer(Player p, PlayerData pd) {
        Game g = pd.getGame();
        int x = p.getLocation().getBlockX();
        int y = p.getLocation().getBlockY();
        int z = p.getLocation().getBlockZ();
        int i = 200000;
        Player player = null;
        for (UUID s : g.getPlayers()) {
            int c;
            Location l;
            Player p2 = Bukkit.getPlayer((UUID)s);
            if (p2 == null || p2.equals((Object)p) || pd.isOnTeam(s) || i <= (c = this.cal((int)((double)x - (l = p2.getLocation()).getX())) + this.cal((int)((double)y - l.getY())) + this.cal((int)((double)z - l.getZ())))) continue;
            player = p2;
            i = c;
        }
        if (player != null) {
            p.setCompassTarget(player.getLocation());
        }
        String[] arrstring = new String[2];
        arrstring[0] = player == null ? "none" : player.getName();
        arrstring[1] = String.valueOf(i);
        return arrstring;
    }
}

