package net.zerotoil.cyberworldreset.events;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.bukkit.Bukkit;
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
        main.events++;
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
        if (main.worlds().getWorlds().containsKey(worldName)) {
            if (main.worlds().getWorld(worldName).isResetting()) {
                player.kickPlayer(main.lang().getMsg("still-regenerating").toString(false));
            }
        }
    }

    public boolean isServerOpen() {
        return serverOpen;
    }

    public void setServerOpen(boolean serverOpen) {
        this.serverOpen = serverOpen;
    }

}
