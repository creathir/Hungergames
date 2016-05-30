package me.minebuilders.hg.listeners;

import java.util.UUID;
import me.minebuilders.hg.Config;
import me.minebuilders.hg.Game;
import me.minebuilders.hg.HG;
import me.minebuilders.hg.PlayerData;
import me.minebuilders.hg.Status;
import me.minebuilders.hg.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class GameListener
implements Listener {
    private HG plugin;
    private String tsn = (Object)ChatColor.GOLD + "TrackingStick " + (Object)ChatColor.GREEN + "Uses: ";
    private ItemStack trackingStick;

    public GameListener(HG plugin) {
        this.plugin = plugin;
        ItemStack it = new ItemStack(Material.STICK, 1);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(String.valueOf(this.tsn) + Config.trackingstickuses);
        it.setItemMeta(im);
        this.trackingStick = it;
    }

    public void dropInv(Player p) {
        ItemStack i;
        PlayerInventory inv = p.getInventory();
        Location l = p.getLocation();
        ItemStack[] arritemStack = inv.getContents();
        int n = arritemStack.length;
        int n2 = 0;
        while (n2 < n) {
            i = arritemStack[n2];
            if (i != null && i.getType() != Material.AIR) {
                l.getWorld().dropItemNaturally(l, i);
            }
            ++n2;
        }
        arritemStack = inv.getArmorContents();
        n = arritemStack.length;
        n2 = 0;
        while (n2 < n) {
            i = arritemStack[n2];
            if (i != null && i.getType() != Material.AIR) {
                l.getWorld().dropItemNaturally(l, i);
            }
            ++n2;
        }
    }

    public void checkStick(Game g) {
        if (Config.playersfortrackingstick == g.getPlayers().size()) {
            for (UUID r : g.getPlayers()) {
                Player p = Bukkit.getPlayer((UUID)r);
                if (p == null) continue;
                Util.scm((CommandSender)p, "&a&l[]------------------------------------------[]");
                Util.scm((CommandSender)p, "&a&l |&3&l   You have been given a player-tracking stick! &a&l |");
                Util.scm((CommandSender)p, "&a&l |&3&l   Swing the stick to track players!                &a&l |");
                Util.scm((CommandSender)p, "&a&l[]------------------------------------------[]");
                p.getInventory().addItem(new ItemStack[]{this.trackingStick});
            }
        }
    }

    @EventHandler
    public void onDIe(PlayerDeathEvent event) {
        final Player p = event.getEntity();
        PlayerData pd = this.plugin.players.get(p.getName());
        if (pd != null) {
            final Game g = pd.getGame();
            p.setHealth(20.0);
            Player killer = p.getKiller();
            if (killer != null) {
                g.msgDef("&l&d" + HG.killmanager.getKillString(p.getName(), (Entity)killer));
            } else {
                g.msgDef("&d" + HG.killmanager.getDeathString(p.getLastDamageCause().getCause(), p.getName()));
            }
            event.setDeathMessage(null);
            event.getDrops().clear();
            this.dropInv(p);
            g.exit(p);
            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, new Runnable(){

                @Override
                public void run() {
                    g.leave(p);
                    GameListener.this.checkStick(g);
                }
            }, 10);
        }
    }

    @EventHandler
    public void onSprint(FoodLevelChangeEvent event) {
        Status st;
        Player p = (Player)event.getEntity();
        if (this.plugin.players.containsKey(p.getName()) && ((st = this.plugin.players.get(p.getName()).getGame().getStatus()) == Status.WAITING || st == Status.COUNTDOWN)) {
            event.setFoodLevel(1);
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
	public void useTrackStick(Player p) {
        ItemStack i = p.getItemInHand();
        ItemMeta im = i.getItemMeta();
        if (im.getDisplayName() != null && im.getDisplayName().startsWith(this.tsn)) {
            int uses = 0;
            uses = Integer.parseInt(im.getDisplayName().replace(this.tsn, ""));
            if (uses == 0) {
                p.sendMessage((Object)ChatColor.RED + "This trackingstick is out of uses!");
            } else {
                boolean foundno = true;
                for (Entity e : p.getNearbyEntities(120.0, 50.0, 120.0)) {
                    if (!(e instanceof Player)) continue;
                    im.setDisplayName(String.valueOf(this.tsn) + (uses - 1));
                    foundno = false;
                    Location l = e.getLocation();
                    int range = (int)p.getLocation().distance(l);
                    Util.msg((CommandSender)p, "&3" + ((Player)e).getName() + "&b is " + range + " blocks away from you:&3 " + this.getDirection(p.getLocation().getBlock(), l.getBlock()));
                    i.setItemMeta(im);
                    p.updateInventory();
                    return;
                }
                if (foundno) {
                    Util.msg((CommandSender)p, (Object)ChatColor.RED + "Couldn't locate any nearby players!");
                }
            }
        }
    }

    public String getDirection(Block block, Block block1) {
        Vector bv = block.getLocation().toVector();
        Vector bv2 = block1.getLocation().toVector();
        float y = (float)this.angle(bv.getX(), bv.getZ(), bv2.getX(), bv2.getZ());
        float cal = y * 10.0f;
        int c = (int)cal;
        if (c <= 1 && c >= -1) {
            return "South";
        }
        if (c > -14 && c < -1) {
            return "SouthWest";
        }
        if (c >= -17 && c <= -14) {
            return "West";
        }
        if (c > -29 && c < -17) {
            return "NorthWest";
        }
        if (c > 17 && c < 29) {
            return "NorthEast";
        }
        if (c <= 17 && c >= 14) {
            return "East";
        }
        if (c > 1 && c < 14) {
            return "SouthEast";
        }
        if (c <= 29 && c >= -29) {
            return "North";
        }
        return "UnKnown";
    }

    public double angle(double d, double e, double f, double g) {
        int x = (int)(f - d);
        int z = (int)(g - e);
        double yaw = Math.atan2(x, z);
        return yaw;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = false)
    public void onAttack(EntityDamageByEntityEvent event) {
        Player p;
        PlayerData pd;
        Entity defender = event.getEntity();
        Entity damager = event.getDamager();
        if (damager instanceof Projectile) {
            damager = (Entity)((Projectile)damager).getShooter();
        }
        if (defender instanceof Player && damager != null && (pd = this.plugin.players.get((p = (Player)defender).getName())) != null) {
            Game g = pd.getGame();
            if (g.getStatus() != Status.RUNNING) {
                event.setCancelled(true);
            } else if (pd.isOnTeam(p.getUniqueId()) && damager instanceof Player && pd.getTeam().isOnTeam(((Player)damager).getUniqueId())) {
                Util.scm((CommandSender)((Player)damager), "&c" + p.getName() + " is on your team!");
                event.setCancelled(true);
            } else if (event.isCancelled()) {
                event.setCancelled(false);
            }
        }
    }

    @EventHandler
    public void onItemUseAttempt(PlayerInteractEvent event) {
        Status st;
        Player p = event.getPlayer();
        if (event.getAction() != Action.PHYSICAL && this.plugin.players.containsKey(p.getName()) && ((st = this.plugin.players.get(p.getName()).getGame().getStatus()) == Status.WAITING || st == Status.COUNTDOWN)) {
            event.setCancelled(true);
            p.sendMessage((Object)ChatColor.RED + "You cannot interact until the game has started!");
        }
    }

    @EventHandler
    public void onPlayerClickLobby(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK)) {
            Sign sign;
            Block b = event.getClickedBlock();
            if (b.getType().equals((Object)Material.WALL_SIGN) && (sign = (Sign)b.getState()).getLine(0).equals((Object)ChatColor.DARK_BLUE + "" + (Object)ChatColor.BOLD + "HungerGames")) {
                Game game = HG.manager.getGame(sign.getLine(1).substring(2));
                if (game == null) {
                    Util.msg((CommandSender)p, (Object)ChatColor.RED + "This arena does not exist!");
                    return;
                }
                if (p.getInventory().getItemInHand().getType() == Material.AIR) {
                    game.join(p);
                } else {
                    Util.msg((CommandSender)p, (Object)ChatColor.RED + "Click the sign with your hand!");
                }
            }
        } else if (event.getAction().equals((Object)Action.LEFT_CLICK_AIR) && p.getInventory().getItemInHand().getType().equals((Object)Material.STICK) && this.plugin.players.containsKey(p.getName())) {
            this.useTrackStick(p);
        }
    }

    @SuppressWarnings("deprecation")
	@EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        Block b = event.getBlock();
        if (HG.manager.isInRegion(b.getLocation())) {
            if (Config.breakblocks && this.plugin.players.containsKey(p.getName())) {
                Game g = this.plugin.players.get(p.getName()).getGame();
                if (g.getStatus() == Status.RUNNING || g.getStatus() == Status.BEGINNING) {
                    if (!Config.blocks.contains(b.getType().getId())) {
                        p.sendMessage((Object)ChatColor.RED + "You cannot edit this block type!");
                        event.setCancelled(true);
                        return;
                    }
                    g.recordBlockPlace(event.getBlockReplacedState());
                    return;
                }
                p.sendMessage((Object)ChatColor.RED + "The game is not running!");
                event.setCancelled(true);
                return;
            }
            if (p.hasPermission("hg.create") && HG.manager.getGame(b.getLocation()).getStatus() != Status.RUNNING) {
                if (b.getType() == Material.CHEST) {
                    HG.manager.getGame(b.getLocation()).addChests(b.getLocation());
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("deprecation")
	@EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Block b = event.getBlock();
        if (HG.manager.isInRegion(b.getLocation())) {
            if (Config.breakblocks && this.plugin.players.containsKey(p.getName())) {
                Game g = this.plugin.players.get(p.getName()).getGame();
                if (g.getStatus() == Status.RUNNING) {
                    if (!Config.blocks.contains(b.getType().getId())) {
                        p.sendMessage((Object)ChatColor.RED + "You cannot edit this block type!");
                        event.setCancelled(true);
                        return;
                    }
                    g.recordBlockBreak(b);
                    return;
                }
                p.sendMessage((Object)ChatColor.RED + "The game is not running!");
                event.setCancelled(true);
                return;
            }
            if (p.hasPermission("hg.create") && HG.manager.getGame(b.getLocation()).getStatus() != Status.RUNNING) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        if (this.plugin.players.containsKey(p.getName()) && this.plugin.players.get(p.getName()).getGame().getStatus() == Status.WAITING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onlogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.players.containsKey(player.getName())) {
            this.plugin.players.get(player.getName()).getGame().leave(player);
        }
    }

}

