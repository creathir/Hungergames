package me.minebuilders.hg.commands;

import me.minebuilders.hg.Game;
import me.minebuilders.hg.HG;
import me.minebuilders.hg.Util;

public class ListCmd extends BaseCmd {

	public ListCmd() {
		forcePlayer = true;
		cmdName = "list";
		forceInGame = true;
		argLength = 1;
	}

	@Override
	public boolean run() {
		String p = "";
		Game g = HG.plugin.players.get(player.getUniqueId()).getGame();
		for (String s : g.getPlayerNames()) {
			p = p + "&3, &b" + s;
		}
		p = p.substring(3);
		Util.scm(player, "&3Players:" + p);
		return true;
	}
}