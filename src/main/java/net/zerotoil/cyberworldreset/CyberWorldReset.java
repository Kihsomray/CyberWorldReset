package net.zerotoil.cyberworldreset;

import me.croabeast.beanslib.Beans;
import net.zerotoil.cyberworldreset.command.CWRCommand;
import net.zerotoil.cyberworldreset.listener.ListenerManager;
import net.zerotoil.cyberworldreset.config.CacheManager;
import net.zerotoil.cyberworldreset.hook.HookManager;
import net.zerotoil.dev.cybercore.CoreSettings;
import net.zerotoil.dev.cybercore.CyberCore;
import net.zerotoil.dev.cybercore.files.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * This class is the main instance of the plugin
 * CyberWorldReset.
 *
 * @author Kihsomray
 */
public class CyberWorldReset extends JavaPlugin {

    private static boolean DEBUG = false;

    private CyberCore core;

    private CacheManager cache;
    private HookManager hooks;
    private ListenerManager listeners;

    private CWRCommand command;

    @Override
    public void onEnable() {

        if (!CyberCore.restrictVersions(13, 20, "XWR", getDescription().getVersion())) return;

        reloadCore();
        loadPlugin();
        core.loadFinish();

    }

    /**
     * Reload the core of the plugin (CyberCore).
     */
    public void reloadCore() {
        core = new CyberCore(this);
        CoreSettings settings = core.coreSettings();
        settings.setBootColor('b');
        settings.setBootLogo(
                "&b╭━━━╮&7╱╱╱&b╭╮&7╱╱╱╱╱╱&b╭╮╭╮╭╮&7╱╱╱╱&b╭╮&7╱╱&b╭┳━━━╮&7╱╱╱╱╱╱╱╱&b╭╮",
                "&b┃╭━╮┃&7╱╱╱&b┃┃&7╱╱╱╱╱╱&b┃┃┃┃┃┃&7╱╱╱╱&b┃┃&7╱╱&b┃┃╭━╮┃&7╱╱╱╱╱╱╱&b╭╯╰╮",
                "&b┃┃&7╱&b╰╋╮&7╱&b╭┫╰━┳━━┳━┫┃┃┃┃┣━━┳━┫┃╭━╯┃╰━╯┣━━┳━━┳━┻╮╭╯",
                "&b┃┃&7╱&b╭┫┃&7╱&b┃┃╭╮┃┃━┫╭┫╰╯╰╯┃╭╮┃╭┫┃┃╭╮┃╭╮╭┫┃━┫━━┫┃━┫┃",
                "&b┃╰━╯┃╰━╯┃╰╯┃┃━┫┃╰╮╭╮╭┫╰╯┃┃┃╰┫╰╯┃┃┃╰┫┃━╋━━┃┃━┫╰╮",
                "&b╰━━━┻━╮╭┻━━┻━━┻╯&7╱&b╰╯╰╯╰━━┻╯╰━┻━━┻╯╰━┻━━┻━━┻━━┻━╯",
                "&7╱╱╱╱&b╭━╯┃  &7Author: &f Kihsomray",
                "&7╱╱╱╱&b╰━━╯  " + this.getDescription().getVersion()
        );

        core.loadStart("plugin-data", "worlds");

    }

    private void loadPlugin() {
        reloadPlugin();
        listeners = new ListenerManager(this);
        command = new CWRCommand(this);

        // Plugin data config.
        final Configuration config = files().getConfig("plugin-data");

        // Temporary fix.
        final ConfigurationSection section = config.getConfigurationSection("player-data.logout-location");
        if (section != null)
            for (String s : section.getKeys(false)) {
                String world = section.getString(s + ".world");
                if (!cache.worlds().getWorlds().containsKey(world)) section.set(s, null);
            }

        // Version update.
        final String version = getDescription().getVersion();
        if (!config.getString("version.current", version).equals(version)) {
            config.set("version.previous", config.getString("version.current"));
            config.set("version.current", version);
            // TODO add reset to "NORMAL" on non-paper forks.
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, () ->{
            try {
                files().get("plugin-data").saveConfig();
            } catch (Exception e) {
                // nothing
            }
        });

    }

    /**
     * Reload the plugin and all cache.
     */
    public void reloadPlugin() {
        cache = new CacheManager(this).reloadConfig().reloadLang();
        hooks = new HookManager(this);
        cache.reloadWorlds();
    }

    @Override
    public void onDisable() {
        cache.worlds().shutdown();
        Bukkit.getScheduler().cancelTasks(this);
        listeners.getQuit().savePlayerData();
    }

    /**
     * Sends a debug message if enabled in config.yml
     *
     * @param onlinePlayers Should it send to online players
     * @param message Message to send
     */
    public static void debug(boolean onlinePlayers, @NotNull String... message) {
        if (!DEBUG) return;
        if (onlinePlayers) for (Player player : Bukkit.getOnlinePlayers()) player.sendMessage("XWR debug: " + Arrays.toString(message));
        else for (String s : message) Bukkit.getLogger().info("[Debug] " + s);
    }

    /**
     * Gets the author of the plugin.
     *
     * @return Author of XenoWorldReset
     */
    public String getAuthors() {
        return this.getDescription().getAuthors().toString().replace("[", "").replace("]", "");
    }

    /**
     * Sends a logger message to console.
     *
     * @param message Message to send in console
     */
    public void logger(String... message) {
        core.logger(message);
    }

    /**
     * Sets if debug messages should be sent.
     *
     * Should only be set when loading the plugin.
     *
     * @param enabled should debug message be sent
     */
    public void setDebug(boolean enabled) {
        DEBUG = enabled;
    }


    /**
     * Gets the core (CyberCore) of the plugin.
     *
     * @return CyberCore instance
     */
    public CyberCore core() {
        return core;
    }

    /**
     * Gets the files of the plugin.
     *
     * @return Loaded files
     */
    public FileManager files() {
        return core.files();
    }

    /**
     * Gets a specific config from files
     *
     * @param config Name of config (without extension)
     * @return Configuration of the file
     */
    public Configuration getConfig(String config) {
        return core.files().getConfig(config);
    }


    /**
     * Gets loaded cache of the plugin.
     *
     * @return Cache manager
     */
    public CacheManager cache() {
        return cache;
    }

    /**
     * Gets loaded hooks of the plugin.
     *
     * @return Hook manager
     */
    public HookManager hooks() {
        return hooks;
    }

    /**
     * Gets loaded listeners of the plugin.
     *
     * @return Listener manager
     */
    public ListenerManager listeners() {
        return listeners;
    }

    /**
     * Gets XWR command handler.
     *
     * @return XWR command handler
     */
    public CWRCommand command() {
        return command;
    }


    /**
     * Checks if a certain plugin is enabled within
     * the Bukkit system.
     *
     * @param plugin Plugin to check
     * @return True if enabled
     */
    public boolean isEnabled(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin) != null;
    }

    /**
     * Colorizes a string with gradient & standard colors.
     *
     * @param string String to colorize.
     * @return Colorized string.
     */
    public static String colorize(final String string) {
        return Beans.colorize(string);
    }

}
