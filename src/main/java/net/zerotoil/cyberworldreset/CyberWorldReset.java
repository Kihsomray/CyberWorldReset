package net.zerotoil.cyberworldreset;

import net.zerotoil.cyberworldreset.cache.Config;
import net.zerotoil.cyberworldreset.cache.Files;
import net.zerotoil.cyberworldreset.cache.Lang;
import net.zerotoil.cyberworldreset.cache.Worlds;
import net.zerotoil.cyberworldreset.commands.CWRCommand;
import net.zerotoil.cyberworldreset.listeners.*;
import net.zerotoil.cyberworldreset.objects.Lag;
import net.zerotoil.cyberworldreset.utilities.LangUtils;
import net.zerotoil.cyberworldreset.utilities.WorldUtils;
import net.zerotoil.cyberworldreset.utilities.ZipUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CyberWorldReset extends JavaPlugin {

    private Files files;
    private Config config;
    private Lang lang;
    private Worlds worlds;

    private CWRCommand cwrCommand;

    private LangUtils langUtils;
    private WorldUtils worldUtils;
    private ZipUtils zipUtils;

    private OnJoin onJoin;
    private OnWorldChange onWorldChange;
    private OnDamage onDamage;
    private OnWorldCreate onWorldCreate;
    private OnWorldSave onWorldSave;


    public Files files() {
        return files;
    }
    public Config config() {
        return config;
    }
    public Lang lang() {
        return lang;
    }
    public Worlds worlds() {
        return worlds;
    }

    public CWRCommand cwrCommand() {
        return cwrCommand;
    }

    public LangUtils langUtils() {
        return langUtils;
    }
    public WorldUtils worldUtils() {
        return worldUtils;
    }
    public ZipUtils zipUtils() {
        return zipUtils;
    }

    public OnJoin onJoin() {
        return onJoin;
    }
    public OnWorldChange onWorldChange() {
        return onWorldChange;
    }
    public OnDamage onDamage() {
        return onDamage;
    }
    public OnWorldCreate onWorldCreate() {
        return onWorldCreate;
    }
    public OnWorldSave onWorldSave() {
        return onWorldSave;
    }

    @Override
    public void onEnable() {

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);

        // utilities
        langUtils = new LangUtils(this);
        worldUtils = new WorldUtils(this);
        zipUtils = new ZipUtils(this);

        // load files/configs into cache
        loadCache();

        // commands
        cwrCommand = new CWRCommand(this);

        onJoin = new OnJoin(this);
        onWorldChange = new OnWorldChange(this);
        onDamage = new OnDamage(this);
        onWorldCreate = new OnWorldCreate(this);
        onWorldSave = new OnWorldSave(this);
        Bukkit.getPluginManager().registerEvents(onJoin, this);
        Bukkit.getPluginManager().registerEvents(onWorldChange, this);
        Bukkit.getPluginManager().registerEvents(onDamage, this);
        Bukkit.getPluginManager().registerEvents(onWorldCreate, this);

    }

    public void loadCache() {
        // load files/configs into cache
        files = new Files(this);
        files.loadFiles();
        config = new Config(this);
        config.loadConfig();
        lang = new Lang(this);
        lang.loadLang();
        worlds = new Worlds(this);
        worlds.loadWorlds();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        worlds.cancelTimers();
        // TODO - Cycle through and disable all timer threads

    }
}
