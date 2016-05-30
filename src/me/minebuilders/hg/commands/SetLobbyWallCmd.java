package me.minebuilders.hg.commands;

import java.util.Set;
import me.minebuilders.hg.Game;
import me.minebuilders.hg.HG;
import me.minebuilders.hg.Util;
import me.minebuilders.hg.commands.BaseCmd;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;

public class SetLobbyWallCmd
extends BaseCmd {
    public SetLobbyWallCmd() {
        this.forcePlayer = true;
        this.cmdName = "setlobbywall";
        this.forceInGame = false;
        this.argLength = 2;
        this.usage = "<&carena-name&b>";
    }

    @Override
    public boolean run() {
        Game g = HG.manager.getGame(this.args[1]);
        if (g != null) {
            Block b = this.player.getTargetBlock((Set<Material>)null, 6);
            if (b.getType() == Material.WALL_SIGN && g.setLobbyBlock((Sign)b.getState())) {
                Location l = b.getLocation();
                HG.arenaconfig.getCustomConfig().set("arenas." + this.args[1] + "." + "lobbysign", (Object)(String.valueOf(l.getWorld().getName()) + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ()));
                HG.arenaconfig.saveCustomConfig();
                Util.msg((CommandSender)this.player, "&aThe lobbyWallSign has been set!");
                HG.manager.checkGame(g, this.player);
            } else {
                Util.msg((CommandSender)this.player, "&cThese signs aren't in correct format!");
                Util.msg((CommandSender)this.player, "&cformat: &6[sign] &c[sign] [sign]");
            }
        } else {
            this.player.sendMessage("This arena does not exist!");
        }
        return true;
    }
}

