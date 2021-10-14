package net.zerotoil.cyberworldreset;

import net.zerotoil.cyberworldreset.addons.Metrics;
import net.zerotoil.cyberworldreset.cache.*;
import net.zerotoil.cyberworldreset.commands.CWRCommand;
import net.zerotoil.cyberworldreset.commands.CWRTabComplete;
import net.zerotoil.cyberworldreset.listeners.*;
import net.zerotoil.cyberworldreset.objects.Lag;
import net.zerotoil.cyberworldreset.utilities.*;
import org.apache.commons.lang.SystemUtils;
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
    public String getEdition() {
        String edition = "Standard";
        if (premium) edition = "Premium";
        return edition;
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

        sendBootMSG();
        long startTime = System.currentTimeMillis();

        premium = false;

        // lag initialize
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);

        loadUtilities();
        loadCache();
        loadListeners();

        // commands
        cwrCommand = new CWRCommand(this);
        cwrTabComplete = new CWRTabComplete(this);
        this.getCommand("cwr").setTabCompleter(cwrTabComplete);

        // addons
        Metrics metrics = new Metrics(this, 13007);

        Bukkit.getLogger().info("[CyberWorldReset] Loaded in " + (System.currentTimeMillis() - startTime) + "ms.");

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
        Bukkit.getLogger().info("[CyberWorldReset] Loading Listeners...");
        onJoin = new OnJoin(this);
        onWorldChange = new OnWorldChange(this);
        onDamage = new OnDamage(this);
        onWorldCreate = new OnWorldCreate(this);
    }

    public void sendBootMSG() {
        if (!SystemUtils.OS_NAME.contains("Windows")) {
            Bukkit.getLogger().info("╭━━━╮╱╱╱╭╮╱╱╱╱╱╱╭╮╭╮╭╮╱╱╱╱╭╮╱╱╭┳━━━╮╱╱╱╱╱╱╱╱╭╮");
            Bukkit.getLogger().info("┃╭━╮┃╱╱╱┃┃╱╱╱╱╱╱┃┃┃┃┃┃╱╱╱╱┃┃╱╱┃┃╭━╮┃╱╱╱╱╱╱╱╭╯╰╮");
            Bukkit.getLogger().info("┃┃╱╰╋╮╱╭┫╰━┳━━┳━┫┃┃┃┃┣━━┳━┫┃╭━╯┃╰━╯┣━━┳━━┳━┻╮╭╯");
            Bukkit.getLogger().info("┃┃╱╭┫┃╱┃┃╭╮┃┃━┫╭┫╰╯╰╯┃╭╮┃╭┫┃┃╭╮┃╭╮╭┫┃━┫━━┫┃━┫┃");
            Bukkit.getLogger().info("┃╰━╯┃╰━╯┃╰╯┃┃━┫┃╰╮╭╮╭┫╰╯┃┃┃╰┫╰╯┃┃┃╰┫┃━╋━━┃┃━┫╰╮");
            Bukkit.getLogger().info("╰━━━┻━╮╭┻━━┻━━┻╯╱╰╯╰╯╰━━┻╯╰━┻━━┻╯╰━┻━━┻━━┻━━┻━╯");
            Bukkit.getLogger().info("╱╱╱╱╭━╯┃  Authors: " + getAuthors());
            Bukkit.getLogger().info("╱╱╱╱╰━━╯  Version: " + this.getDescription().getVersion() + "-BETA [" + getEdition() + "]");
        }
    }

    public String getAuthors() {
        return this.getDescription().getAuthors().toString().replace("[", "").replace("]", "");
    }

    @Override
    public void onDisable() {
        worlds.cancelTimers();
    }

    public int getVersion() {
        return Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
    }

}
