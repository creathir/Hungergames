package me.minebuilders.hg.commands;

import java.util.UUID;
import me.minebuilders.hg.Game;
import me.minebuilders.hg.HG;
import me.minebuilders.hg.Status;
import me.minebuilders.hg.Util;
import me.minebuilders.hg.commands.BaseCmd;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DeleteCmd
extends BaseCmd {
    public DeleteCmd() {
        this.forcePlayer = false;
        this.cmdName = "delete";
        this.forceInGame = false;
        this.argLength = 2;
        this.usage = "<&carena-name&b>";
    }

    @Override
    public boolean run() {
        Game g = HG.manager.getGame(this.args[1]);
        if (g != null) {
            try {
                Util.msg(this.sender, "&aAttempting to delete " + g.getName() + "!");
                if (g.getStatus() == Status.BEGINNING || g.getStatus() == Status.RUNNING) {
                    Util.msg(this.sender, "  &7- &cGame running! &aStopping..");
                    g.forceRollback();
                    g.stop();
                }
                if (!g.getPlayers().isEmpty()) {
                    Util.msg(this.sender, "  &7- &c&cPlayers detected! &aKicking..");
                    for (UUID s : g.getPlayers()) {
                        Player p = Bukkit.getPlayer(s);
                        if (p == null) continue;
                        g.leave(p);
                    }
                }
                HG.arenaconfig.getCustomConfig().set("arenas." + this.args[1], (Object)null);
                HG.arenaconfig.saveCustomConfig();
                HG.plugin.games.remove(g);
                Util.msg(this.sender, "&aSuccessfully deleted Hungergames arena!");
            }
            catch (Exception e) {
                Util.msg(this.sender, "&cFailed to delete arena!");
            }
        } else {
            this.sender.sendMessage("This arena does not exist!");
        }
        return true;
    }
}

