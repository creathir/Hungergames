package me.minebuilders.hg.commands;

import java.util.Iterator;
import java.util.UUID;
import me.minebuilders.hg.Config;
import me.minebuilders.hg.Game;
import me.minebuilders.hg.HG;
import me.minebuilders.hg.PlayerData;
import me.minebuilders.hg.Team;
import me.minebuilders.hg.Util;
import me.minebuilders.hg.commands.BaseCmd;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCmd
extends BaseCmd {
    public TeamCmd() {
        this.forcePlayer = true;
        this.cmdName = "team";
        this.forceInGame = true;
        this.argLength = 2;
        this.usage = "<&cinvite&b/&caccept&b>";
    }

    @Override
    public boolean run() {
        PlayerData pd = HG.plugin.players.get(this.player.getName());
        Game g = pd.getGame();
        if (this.args[1].equalsIgnoreCase("invite")) {
            if (this.args.length >= 3) {
                Team t;
                Player p = null;
                for (Player tmpPlayer : Bukkit.getOnlinePlayers()) {
                    if (!tmpPlayer.getName().equalsIgnoreCase(this.args[2])) continue;
                    p = tmpPlayer;
                    break;
                }
                if (p == null || !g.getPlayers().contains(p.getName())) {
                    Util.msg((CommandSender)this.player, "&c" + this.args[2] + " Is not available!");
                    return true;
                }
                if (pd.getTeam() != null) {
                    t = pd.getTeam();
                    if (!t.getLeader().equals((Object)this.player)) {
                        Util.msg((CommandSender)this.player, "&cOnly the leader can invite other players!");
                        return true;
                    }
                    if (t.isOnTeam(p.getUniqueId())) {
                        Util.msg((CommandSender)this.player, "&c" + this.args[2] + " &3is already on a team!");
                        return true;
                    }
                    if (t.getPlayers().size() + t.getPenders().size() >= Config.maxTeam) {
                        Util.msg((CommandSender)this.player, "&cYou've hit the max team limit!");
                        return true;
                    }
                    HG.plugin.players.get(p.getName()).setTeam(t);
                    t.invite(p);
                    Util.msg((CommandSender)this.player, "&c" + p.getName() + " &3Has been invited!");
                    return true;
                }
                if (HG.plugin.players.get(p.getName()).isOnTeam(p.getUniqueId())) {
                    Util.msg((CommandSender)this.player, "&c" + this.args[2] + " &3is already on a team!");
                    return true;
                }
                t = new Team(this.player);
                HG.plugin.players.get(p.getName()).setTeam(t);
                pd.setTeam(t);
                t.invite(p);
                Util.msg((CommandSender)this.player, "&c" + p.getName() + " &3Has been invited!");
                return true;
            }
            Util.msg((CommandSender)this.player, "&cWrong Usage: &3/hg &bteam invite <&cname&b>");
        } else if (this.args[1].equalsIgnoreCase("accept")) {
            Team t = HG.plugin.players.get(this.player.getName()).getTeam();
            if (t == null) {
                Util.msg((CommandSender)this.player, "&cYou have no pending invites...");
                return true;
            }
            if (t.getPenders().contains(this.player.getName())) {
                t.acceptInvite(this.player);
                Iterator<UUID> iterator = t.getPlayers().iterator();
                if (iterator.hasNext()) {
                    UUID s = iterator.next();
                    Player p = Bukkit.getPlayer((UUID)s);
                    if (p != null) {
                        Util.scm((CommandSender)p, "&6*&b&m                                                                             &6*");
                        Util.scm((CommandSender)p, (Object)ChatColor.WHITE + this.player.getName() + " &3Just joined your team!");
                        Util.scm((CommandSender)p, "&6*&b&m                                                                             &6*");
                    }
                    return true;
                }
                return true;
            }
        } else {
            Util.scm((CommandSender)this.player, "&c" + this.args[1] + " is not a valid command!");
            Util.scm((CommandSender)this.player, "&cValid arguments: &6invite&c, &6accept ");
        }
        return true;
    }
}

