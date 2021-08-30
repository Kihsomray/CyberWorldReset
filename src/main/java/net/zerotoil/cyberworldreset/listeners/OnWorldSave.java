package net.zerotoil.cyberworldreset.listeners;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.ArrayList;

public class OnWorldSave implements Listener {

    final private CyberWorldReset main;
    private ArrayList<String> worldToBackup = new ArrayList<>();

    public OnWorldSave(CyberWorldReset main) {
        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {

        String world = event.getWorld().getName();
        if (worldToBackup.contains(world)) {

            try {
                main.zipUtils().zip(world);
                main.lang().getMsg("save-success").send(null, true, new String[]{"world"}, new String[]{world});
            } catch (Exception e) {
                main.lang().getMsg("save-failed").send(null, true, new String[]{"world"}, new String[]{world});
                e.printStackTrace();
            }

            worldToBackup.remove(world);


        }

    }

    public void addWorldToBackup(String world) {
        if (!worldToBackup.contains(world)) worldToBackup.add(world);
    }

}
