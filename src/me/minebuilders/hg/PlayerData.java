package me.minebuilders.hg;

import java.util.UUID;
import me.minebuilders.hg.Game;
import me.minebuilders.hg.Team;
import me.minebuilders.hg.Util;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerData {
    private ItemStack[] inv;
    private ItemStack[] equip;
    private int exp;
    private GameMode mode;
    private Team team;
    private Game game;

    public PlayerData(Player p, Game game) {
        this.game = game;
        this.inv = p.getInventory().getContents();
        this.equip = p.getInventory().getArmorContents();
        this.exp = (int)p.getExp();
        this.mode = p.getGameMode();
        Util.clearInv(p);
    }

    @SuppressWarnings("deprecation")
	public void restore(Player p) {
        Util.clearInv(p);
        p.setExp(0.0f);
        p.giveExp(this.exp);
        p.getInventory().setContents(this.inv);
        p.getInventory().setArmorContents(this.equip);
        p.setGameMode(this.mode);
        p.updateInventory();
    }

    public boolean isOnTeam(UUID s) {
        if (this.team != null && this.team.isOnTeam(s)) {
            return true;
        }
        return false;
    }

    public Game getGame() {
        return this.game;
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}

