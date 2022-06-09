package net.zerotoil.cyberworldreset;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldguard.WorldGuard;
import net.zerotoil.cyberworldreset.addons.Metrics;
import net.zerotoil.cyberworldreset.addons.PlaceholderAPI;
import net.zerotoil.cyberworldreset.cache.*;
import net.zerotoil.cyberworldreset.commands.CWRCommand;
import net.zerotoil.cyberworldreset.commands.CWRTabComplete;
import net.zerotoil.cyberworldreset.events.*;
import net.zerotoil.cyberworldreset.objects.Lag;
import net.zerotoil.cyberworldreset.utilities.*;
import org.apache.commons.lang.SystemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class CyberWorldReset extends JavaPlugin {

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

    private boolean placeholderAPIEnabled;
    private boolean multiverseEnabled;
    private MultiverseCore multiverseCore;
    private WorldGuard worldGuard;

    private int events = 0;

    @Override
    public void onEnable() {

        if (getVersion() > 19) {
            Bukkit.getLogger().severe("CWR v" + getDescription().getVersion() + " does not support MC version 1.20 and newer. Please update!");
            return;
        }
        if (getVersion() < 8) {
            Bukkit.getLogger().severe("CWR v" + getDescription().getVersion() + " does not support Minecraft versions older than 1.8");
            return;
        }

        sendBootMSG();
        long startTime = System.currentTimeMillis();

        // lag initialize
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);

        loadUtilities();
        loadCache();
        loadEvents();

        // commands
        cwrCommand = new CWRCommand(this);
        cwrTabComplete = new CWRTabComplete(this);

        // addons
        new Metrics(this, 13007, this);

        // multiverse
        multiverseEnabled = getServer().getPluginManager().isPluginEnabled("Multiverse-Core");
        if (multiverseEnabled) {
            multiverseCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
            logger("&7CWR recognizes Multiverse is &aENABLED.");
            logger("");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            new PlaceholderAPI(this).register();
        } else placeholderAPIEnabled = false;

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && (getVersion() > 12)) worldGuard = WorldGuard.getInstance();

        // final message
        logger("&7Loaded &bCWR v" + getDescription().getVersion() + "&7 in &a" +
                (System.currentTimeMillis() - startTime) + "ms&7.");
        logger("&b―――――――――――――――――――――――――――――――――――――――――――――――");
    }

    public void logger(String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        if (SystemUtils.OS_NAME.contains("Windows")) {
            msg = ChatColor.stripColor(msg);
            msg = msg.replace("―", "-");
        }
        getLogger().info(msg);
    }

    private void loadUtilities() {
        // load utilities
        langUtils = new LangUtils(this);
        worldUtils = new WorldUtils();
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

    private void loadEvents() {
        // load listeners
        logger("&bLoading events...");
        long startTime = System.currentTimeMillis();
        onJoin = new OnJoin(this);
        onWorldChange = new OnWorldChange(this);
        onDamage = new OnDamage(this);
        onWorldCreate = new OnWorldCreate(this);
        logger("&7Loaded &e" + events + " &7events in &a" + (System.currentTimeMillis() - startTime) + "ms&7.");
        logger("");
    }

    // needs to be sent before langUtils, therefore
    // I cannot relocate it to there (yet).
    public void sendBootMSG() {
        logger("&b―――――――――――――――――――――――――――――――――――――――――――――――");
        String author = "&7Authors: &f" + getAuthors();
        String version = "&7Version: &f" + this.getDescription().getVersion() + " [STANDARD]";
        if (!SystemUtils.OS_NAME.contains("Windows")) {
            logger("&b╭━━━╮&7╱╱╱&b╭╮&7╱╱╱╱╱╱&b╭╮╭╮╭╮&7╱╱╱╱&b╭╮&7╱╱&b╭┳━━━╮&7╱╱╱╱╱╱╱╱&b╭╮");
            logger("&b┃╭━╮┃&7╱╱╱&b┃┃&7╱╱╱╱╱╱&b┃┃┃┃┃┃&7╱╱╱╱&b┃┃&7╱╱&b┃┃╭━╮┃&7╱╱╱╱╱╱╱&b╭╯╰╮");
            logger("&b┃┃&7╱&b╰╋╮&7╱&b╭┫╰━┳━━┳━┫┃┃┃┃┣━━┳━┫┃╭━╯┃╰━╯┣━━┳━━┳━┻╮╭╯");
            logger("&b┃┃&7╱&b╭┫┃&7╱&b┃┃╭╮┃┃━┫╭┫╰╯╰╯┃╭╮┃╭┫┃┃╭╮┃╭╮╭┫┃━┫━━┫┃━┫┃");
            logger("&b┃╰━╯┃╰━╯┃╰╯┃┃━┫┃╰╮╭╮╭┫╰╯┃┃┃╰┫╰╯┃┃┃╰┫┃━╋━━┃┃━┫╰╮");
            logger("&b╰━━━┻━╮╭┻━━┻━━┻╯&7╱&b╰╯╰╯╰━━┻╯╰━┻━━┻╯╰━┻━━┻━━┻━━┻━╯");
            logger("&7╱╱╱╱&b╭━╯┃  " + author);
            logger("&7╱╱╱╱&b╰━━╯  " + version);
        } else {
            logger("_________  __      ____________ ");
            logger("\\_   ___ \\/  \\    /  \\______   \\");
            logger("/    \\  \\/\\   \\/\\/   /|       _/");
            logger("\\     \\____\\        / |    |   \\");
            logger(" \\______  / \\__/\\  /  |____|_  /");
            logger("        \\/       \\/          \\/ ");
            logger(ChatColor.stripColor(author));
            logger(ChatColor.stripColor(version));
        }
        logger("&b―――――――――――――――――――――――――――――――――――――――――――――――");
        logger("");
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

    public void addEvent() {
        events++;
    }

    // field getters
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

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
    public boolean isMultiverseEnabled() {
        return multiverseEnabled;
    }
    public MultiverseCore multiverse() {
        return multiverseCore;
    }
    public WorldGuard worldGuard() {
        return worldGuard;
    }

}
