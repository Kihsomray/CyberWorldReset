package net.zerotoil.cyberworldreset.old.events;

import net.zerotoil.cyberworldreset.old.CyberWorldReset;
import net.zerotoil.cyberworldreset.old.objects.WorldObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnJoin implements Listener {

    private CyberWorldReset main;
    private boolean serverOpen;

    public OnJoin(CyberWorldReset main) {

        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
        main.addEvent();
        serverOpen = true;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void preJoin(AsyncPlayerPreLoginEvent event) {

        if (!serverOpen){
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, main.lang().
                    getMsg("still-regenerating").toString(false));
        }

    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        if (!main.worlds().getWorlds().containsKey(worldName)) return;

        WorldObject wo = main.worlds().getWorld(worldName);

        if (wo.isResetting()) {
            player.kickPlayer(main.lang().getMsg("still-regenerating").toString(false));
            return;
        }

        if (main.config().isUnsafeLocationFix() && main.getVersion() > 12) {

            // nether ceiling prevention
            if (player.getWorld().getEnvironment() == World.Environment.NETHER) return;

            Location lAt = player.getLocation();
            Location lDown = new Location(wo.getWorld(), lAt.getX(), lAt.getY() - 1, lAt.getZ());
            Location lUp = new Location(wo.getWorld(), lAt.getX(), lAt.getY() + 1, lAt.getZ());

            if (!lAt.getBlock().getType().isAir() || !lUp.getBlock().getType().isAir() || lDown.getBlock().isPassable())
                player.teleport(new Location (wo.getWorld(), lAt.getX(), wo.getWorld().getHighestBlockYAt(lAt.getBlockX(), lAt.getBlockZ()), lAt.getZ()));

        }

    }

    public boolean isServerOpen() {
        return serverOpen;
    }

    public void setServerOpen(boolean serverOpen) {
        this.serverOpen = serverOpen;
    }

}
