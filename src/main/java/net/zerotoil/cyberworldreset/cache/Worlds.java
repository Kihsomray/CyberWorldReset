package net.zerotoil.cyberworldreset.cache;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import net.zerotoil.cyberworldreset.objects.WorldObject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Worlds {

    private CyberWorldReset main;
    private HashMap<String, WorldObject> worlds = new HashMap<>();
    private Configuration config;

    public Worlds(CyberWorldReset main) {

        this.main = main;
        config = main.files().getConfig("worlds");

    }

    public void loadWorlds(boolean newWorlds) {

        if (!worlds.isEmpty() && !newWorlds) worlds.clear();

        if (!config.getConfigurationSection("worlds").getKeys(false).isEmpty()) {

            for (String worldName : config.getConfigurationSection("worlds").getKeys(false)) {

                // skips already loaded worlds
                if (worlds.containsKey(worldName) && newWorlds) continue;

                // is it an actual world?
                if (!main.worldUtils().isWorld(worldName)) {

                    Bukkit.getLogger().warning(main.langUtils().getLang("The world " + worldName + " is not an existing world! Disabling this world.",
                            "El mundo " + worldName + " no es un mundo existente. Deshabiltando el mundo.",
                            "Мир " + worldName + " не существует! Этот мир не будет использоваться."));
                    continue;

                }

                // if default world
                if (worldName.equalsIgnoreCase(main.worldUtils().getLevelName())) {

                    Bukkit.getLogger().warning(main.langUtils().getLang("The world " + worldName + " is a default world! Disabling this world.",
                            "El mundo " + worldName + " es un mundo predeterminado. Deshabiltando el mundo.",
                            "Мир " + worldName + " - это мир по умолчанию! Этот мир не будет использоваться."));
                    continue;

                }

                // makes a new world object
                WorldObject worldObject = new WorldObject(main, worldName);
                String settings = worldName + ".settings.";

                // intervals/times
                if (isSet(settings + "time")) {

                    worldObject.setTime(config.getStringList("worlds." + settings + "time"));

                } else {

                    Bukkit.getLogger().warning(main.langUtils().getLang("The world " + worldName + " does not have any times set. Disabling this world.",
                            "El mundo " + worldName + " no tiene tiempo establecido. Deshabiltando el mundo.",
                            "Мир " + worldName + " не имеет времени. Этот мир не будет использоваться."));
                    continue;

                }

                // reset message
                if (isSet(settings + "message")) {

                    worldObject.setMessage(main.langUtils().convertList(config, "worlds." + worldName + ".settings.message"));

                }

                // pre-converted seed
                if (isSet(settings + "seed")) {
                    String path = "worlds." + settings + "seed";
                    if (config.isLong(path) || config.isInt(path)) {
                        worldObject.setSeed(config.getLong(path) + "");
                    } else {
                        worldObject.setSeed(config.getString(path));
                    }

                }

                if (isSet(settings + "safe-world.enabled")) {

                    if (config.getBoolean("worlds." + settings + "safe-world.enabled")) {

                        worldObject.setSafeWorldEnabled(true);

                        // safe world name
                        if (isSet(settings + "safe-world.world")) {
                            worldObject.setSafeWorld(config.getString("worlds." + settings + "safe-world.world"));
                        } else {
                            worldObject.setSafeWorldEnabled(false);
                        }
                        if (isSet(settings + "safe-world.delay")) {
                            worldObject.setSafeWorldDelay(config.getLong("worlds." + settings + "safe-world.delay"));
                        } else {
                            worldObject.setSafeWorldDelay(-1);
                        }
                        if (isSet(settings + "safe-world.spawn")) {
                            worldObject.setSafeWorldSpawn(config.getString("worlds." + settings + "safe-world.spawn"));
                        } else {
                            worldObject.setSafeWorldSpawn("default");
                        }

                    }

                }

                if (isSet(settings + "warning.enabled")) {

                    if (config.getBoolean("worlds." + settings + "warning.enabled")) {

                        worldObject.setWarningEnabled(true);

                        if (isSet(settings + "warning.message")) worldObject.setWarningMessage(main.langUtils().convertList(config, "worlds." + settings + "warning.message"));

                        if (isSet(settings + "warning.time")) worldObject.setWarningTime(config.getLongList("worlds." + settings + "warning.time"));


                    }

                }

                if (isSet(settings + "commands")) {
                    worldObject.setCommands(main.langUtils().convertList(config, "worlds." + settings + "commands"));
                }

                if (isSet(settings + "time")) {
                    worldObject.setTime(main.langUtils().convertList(config, "worlds." + settings + "time"));
                }

                if (isSet(worldName + ".last-saved")) {
                    worldObject.setLastSaved(config.getBoolean("worlds." + worldName + ".last-saved"));
                }

                if (isSet(worldName + ".enabled")) {
                    worldObject.setEnabled(config.getBoolean("worlds." + worldName + ".enabled"));
                }

                worlds.put(worldName, worldObject);
                getWorld(worldName).loadTimedResets();

            }

        }

    }

    public void createWorld(String worldName, Player sender) {

        ConfigurationSection cS = config.getConfigurationSection("worlds");
        String s = worldName + ".settings.";
        String safe = s + "safe-world.";
        String warn = s + "warning.";
        cS.set(worldName + ".enabled", false);
        cS.set(worldName + ".last-saved", false);
        cS.set(s + "time", new String[0]);
        cS.set(s + "message", "");
        cS.set(s + "seed", "DEFAULT");
        cS.set(safe + "enabled", false);
        cS.set(safe + "world", "");
        cS.set(safe + "delay", 5);
        cS.set(safe + "spawn", "DEFAULT");
        cS.set(warn + "enabled", false);
        cS.set(warn + "warning", "&cWarning: resetting the world {world} in {time}.");
        cS.set(warn + "time", new int[]{300, 60, 30, 10, 3, 2, 1});
        cS.set(s + "commands", new String[0]);
        try {
            config.set("worlds", cS);
            main.files().get("worlds").saveConfig();
            loadWorlds(true);
            main.lang().getMsg("setup-created").send(sender, true, new String[]{"world"}, new String[]{worldName});
        } catch (Exception e) {
            e.printStackTrace();
            main.lang().getMsg("world-create-failed").send(sender, true, new String[]{"world"}, new String[]{worldName});
        }

    }

    public void cancelTimers() {
        if (worlds.isEmpty()) return;
        for (WorldObject worldObject : worlds.values()) {
            worldObject.cancelTimers();
        }
    }

    private boolean isSet(String location) {
        return config.isSet("worlds." + location);
    }

    public WorldObject getWorld(String name) {
        return worlds.get(name);
    }
    public HashMap<String, WorldObject> getWorlds() {
        return worlds;
    }

}
