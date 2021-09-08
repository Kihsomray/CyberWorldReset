package net.zerotoil.cyberworldreset.listeners;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class OnJoin implements Listener {

    private CyberWorldReset main;
    private boolean serverOpen;

    public OnJoin(CyberWorldReset main) {

        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
        serverOpen = true;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void preJoin(AsyncPlayerPreLoginEvent event) {

        if (!serverOpen){
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, main.lang().
                    getMsg("still-regenerating").toString(false));
        }

    }

    public boolean isServerOpen() {
        return serverOpen;
    }

    public void setServerOpen(boolean serverOpen) {
        this.serverOpen = serverOpen;
    }

}
