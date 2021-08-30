package net.zerotoil.cyberworldreset.listeners;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class OnWorldCreate implements Listener {

    private CyberWorldReset main;

    public OnWorldCreate(CyberWorldReset main) {
        this.main = main;
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onWorldCreate(WorldInitEvent event) {
        if (!main.worlds().getWorlds().containsKey(event.getWorld().getName())) {
            main.worlds().cancelTimers();
            main.loadCache();
        }
        System.out.println("1");
        // if server is open and world is not one that is regenerating
        if (main.onJoin().isServerOpen() && !main.onWorldChange().getClosedWorlds().contains(event.getWorld().getName())) return;
        System.out.println("2");
        boolean loadSpawn = false;
        System.out.println("3");
        if (main.config().getLoadingType().matches("(?i)STANDARD")) loadSpawn = true;
        System.out.println(loadSpawn);
        event.getWorld().setKeepSpawnInMemory(loadSpawn);
        System.out.println("5");
        event.getWorld().setAutoSave(loadSpawn);
        System.out.println("6");
    }

}
