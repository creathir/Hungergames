package me.minebuilders.hg;

import java.util.HashMap;
import java.util.UUID;
import me.minebuilders.hg.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class SBDisplay {
    private ScoreboardManager manager = Bukkit.getScoreboardManager();
    private Scoreboard board = this.manager.getNewScoreboard();
    private Objective ob = this.board.registerNewObjective((Object)ChatColor.GREEN + "Players-Alive:", "dummy");
    private HashMap<UUID, Scoreboard> score = new HashMap<UUID, Scoreboard>();
    private Game g;

    public SBDisplay(Game g) {
        this.ob.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.ob.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "HungerGames");
        this.g = g;
    }

    public void setAlive() {
        Score score = this.ob.getScore((Object)ChatColor.GREEN + "Players-Alive:");
        score.setScore(this.g.getPlayers().size());
    }

    public void resetAlive() {
        this.board.resetScores((Object)ChatColor.GREEN + "Players-Alive:");
        this.score.clear();
    }

    public void setSB(Player p) {
        this.score.put(p.getUniqueId(), p.getScoreboard());
        p.setScoreboard(this.board);
    }

    public void restoreSB(Player p) {
        if (this.score.get(p.getUniqueId()) == null) {
            p.setScoreboard(this.manager.getNewScoreboard());
        } else {
            p.setScoreboard(this.score.get(p.getUniqueId()));
            this.score.remove(p.getUniqueId());
        }
    }
}

