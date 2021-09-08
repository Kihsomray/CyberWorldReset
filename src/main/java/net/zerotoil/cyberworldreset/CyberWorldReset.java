package net.zerotoil.cyberworldreset;

import net.zerotoil.cyberworldreset.cache.*;
import net.zerotoil.cyberworldreset.commands.CWRCommand;
import net.zerotoil.cyberworldreset.commands.CWRTabComplete;
import net.zerotoil.cyberworldreset.listeners.*;
import net.zerotoil.cyberworldreset.objects.Lag;
import net.zerotoil.cyberworldreset.utilities.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CyberWorldReset extends JavaPlugin {

    private boolean premium;

    private Files files;
    private Config config;
    private Lang lang;
    private Worlds worlds;

    private CWRCommand cwrCommand;
    private CWRTabComplete cwrTabComplete;

    private LangUtils langUtils;
    private WorldUtils worldUtils;
    private ZipUtils zipUtils;

    private OnJoin onJoin;
    private OnWorldChange onWorldChange;
    private OnDamage onDamage;
    private OnWorldCreate onWorldCreate;


    public boolean isPremium() {
        return premium;
    }

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
    public CWRTabComplete cwrTabComplete() {
        return cwrTabComplete;
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

    @Override
    public void onEnable() {

        premium = true;

        // lag initialize
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);

        loadUtilities();
        loadCache();
        loadListeners();

        // commands
        cwrCommand = new CWRCommand(this);
        cwrTabComplete = new CWRTabComplete(this);
        this.getCommand("cwr").setTabCompleter(cwrTabComplete);

    }

    private void loadUtilities() {
        // load utilities
        langUtils = new LangUtils(this);
        worldUtils = new WorldUtils(this);
        zipUtils = new ZipUtils(this);
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
        worlds.loadWorlds(false);
    }

    private void loadListeners() {
        // load listeners
        onJoin = new OnJoin(this);
        onWorldChange = new OnWorldChange(this);
        onDamage = new OnDamage(this);
        onWorldCreate = new OnWorldCreate(this);
    }

    @Override
    public void onDisable() {
        worlds.cancelTimers();
    }

    public int getVersion() { // my name is taquito with ñ
        return Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
    }

}
