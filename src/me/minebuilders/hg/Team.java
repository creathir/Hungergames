package me.minebuilders.hg;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.minebuilders.hg.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Team {
    private Player leader;
    private List<UUID> players = new ArrayList<UUID>();
    private List<UUID> pending = new ArrayList<UUID>();

    public Team(Player leader) {
        this.leader = leader;
        this.players.add(leader.getUniqueId());
    }

    public void invite(Player p) {
        Util.scm((CommandSender)p, "&6*&b&m                                                                             &6*");
        Util.scm((CommandSender)p, "| &f" + this.leader.getName() + " &3Just invited you to a &fTeam&3!");
        Util.scm((CommandSender)p, "| &3Type &f/hg team accept &3To join!");
        Util.scm((CommandSender)p, "&6*&b&m                                                                             &6*");
        this.pending.add(p.getUniqueId());
    }

    public void acceptInvite(Player p) {
        this.pending.remove(p.getUniqueId());
        this.players.add(p.getUniqueId());
        Util.msg((CommandSender)p, "&3You successfully joined this team!");
    }

    public boolean isOnTeam(UUID p) {
        return this.players.contains(p);
    }

    public boolean isPending(String p) {
        return this.pending.contains(p);
    }

    public List<UUID> getPlayers() {
        return this.players;
    }

    public List<UUID> getPenders() {
        return this.pending;
    }

    public Player getLeader() {
        return this.leader;
    }
}

