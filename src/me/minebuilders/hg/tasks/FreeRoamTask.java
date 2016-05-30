package me.minebuilders.hg.tasks;

import java.util.UUID;
import me.minebuilders.hg.Game;
import me.minebuilders.hg.HG;
import me.minebuilders.hg.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FreeRoamTask
implements Runnable {
    private Game game;
    private int id;

    public FreeRoamTask(Game g) {
        this.game = g;
        for (UUID s : g.getPlayers()) {
            Player p = Bukkit.getPlayer((UUID)s);
            if (p == null) continue;
            Util.scm((CommandSender)p, "&4[]---------[ &6&lThe game has started! &4]---------[]");
            Util.scm((CommandSender)p, " &e You have " + g.getRoamTime() + " seconds to roam without taking damage!");
            p.setHealth(20.0);
            p.setFoodLevel(20);
            g.unFreeze(p);
        }
        this.id = Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)HG.plugin, (Runnable)this, (long)g.getRoamTime() * 20);
    }

    @Override
    public void run() {
        this.game.msgAll("&c&lFree-Roam is over, PVP is now enabled!");
        this.game.startGame();
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(this.id);
    }
}

