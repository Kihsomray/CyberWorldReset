package net.zerotoil.cyberworldreset.events;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;

public class OnWorldChange implements Listener {

    private CyberWorldReset main;
    private List<String> closedWorlds = new ArrayList<>();

    public OnWorldChange(CyberWorldReset main) {
        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
        main.addEvent();
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerTeleportEvent event) {

        if (closedWorlds.contains(event.getTo().getWorld().getName())) {
            main.lang().getMsg("cancel-teleport").send(event.getPlayer(), true, new String[]{"world"}, new String[]{event.getTo().getWorld().getName()});
            event.setCancelled(true);
        }

    }

    public List<String> getClosedWorlds() {
        return closedWorlds;
    }

    public void setClosedWorlds(List<String> closedWorlds) {
        this.closedWorlds = closedWorlds;
    }
    public void addClosedWorld(String worldName) {
        closedWorlds.add(worldName);
    }
    public void removeClosedWorld(String worldName) {

        if (closedWorlds.contains(worldName)) {
            closedWorlds.remove(worldName);
        }

    }

}
