package net.zerotoil.cyberworldreset.cache;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.Arrays;

public class Config {

    private CyberWorldReset main;
    private Configuration config;
    private boolean updateConfig;
    private boolean updateLang;
    private String lang;

    private boolean saveWorldBeforeReset;

    private String loadingType;
    private long loadingDelay;

    private long timerLoadDelay;
    private long worldResetDelay;
    private int loadRadius;

    private boolean recursiveTeleportEnabled;
    private long recursiveTeleportMilliseconds;

    private boolean detailedMessages;

    private boolean confirmationEnabled;
    private long confirmationSeconds;

    public Config(CyberWorldReset main) {

        this.main = main;
        lang = "en";
        config = main.files().get("config").getConfig();
        // internal language module
        if (isSet("lang")) {
            lang = config.getString("config.lang");
            if (!lang.matches("(?i)en|es|ru")) lang = "en";
        }

    }

    public void loadConfig() {

        updateConfig = getBoolean("auto-update-configs.config", true);
        updateLang = getBoolean("auto-update-configs.lang", true);
        if (updateConfig) main.files().get("config").updateConfig();

        confirmationEnabled = getBoolean("confirmation.enabled", true);
        if (confirmationEnabled) confirmationSeconds = getLong("confirmation.seconds", 15);

        saveWorldBeforeReset = getBoolean("save-world-before-reset", false);
        loadingType = getString("loading-type", "SAFE", new ArrayList<>(Arrays.asList("STANDARD", "NORMAL", "SAFE", "ULTRA-SAFE", "FAST", "ULTRA-FAST")));
        loadingDelay = 5;
        if (loadingType.matches("(?i)FAST")) loadingDelay = 3;
        if (loadingType.matches("(?i)SAFE")) loadingDelay = 8;
        if (loadingType.matches("(?i)ULTRA-SAFE")) loadingDelay = 15;
        timerLoadDelay = getLong("timer-load-delay", 10);
        worldResetDelay = getLong("world-reset-delay", 750);
        loadRadius = Math.max(Math.max(Math.min(getInt("loading-radius", 5), 32), 1), main.getServer().getViewDistance());

        recursiveTeleportEnabled = getBoolean("recursive-teleporting.enabled", true);
        if (recursiveTeleportEnabled) recursiveTeleportMilliseconds = getLong("recursive-teleporting.milliseconds", 10);

        detailedMessages = getBoolean("detailed-messages", true);

    }

    // private
    private boolean isSet(String path) {
        return config.isSet("config." + path);
    }
    private boolean getBoolean(String path, boolean defaultValue) {
        if (isSet(path)) return config.getBoolean("config." + path);
        return defaultValue;
    }
    private long getLong(String path, long defaultValue) {
        if (isSet(path)) return config.getLong("config." + path);
        return defaultValue;
    }
    private int getInt(String path, int defaultValue) {
        if (isSet(path)) return config.getInt("config." + path);
        return defaultValue;
    }
    private String getString(String path, String defaultValue, ArrayList<String> mustInclude) {
        String temp;
        if (isSet(path)) {
            temp = config.getString("config." + path);
            if (mustInclude.isEmpty()) return temp;
            if (mustInclude.contains(temp)) return temp;
        }

        return defaultValue;
    }

    // public
    public Configuration getConfig() {
        return this.config;
    }
    public boolean isUpdateConfig() {
        return updateConfig;
    }
    public boolean isUpdateLang() {
        return updateLang;
    }
    public String getLang() {
        return lang;
    }
    public boolean isConfirmationEnabled() {
        return confirmationEnabled;
    }
    public long getConfirmationSeconds() {
        return confirmationSeconds;
    }
    public boolean isSaveWorldBeforeReset() {
        return saveWorldBeforeReset;
    }
    public String getLoadingType() {
        return loadingType;
    }
    public long getTimerLoadDelay() {
        return timerLoadDelay;
    }
    public boolean isRecursiveTeleportEnabled() {
        return recursiveTeleportEnabled;
    }
    public long getRecursiveTeleportMilliseconds() {
        return recursiveTeleportMilliseconds;
    }
    public boolean isDetailedMessages() {
        return detailedMessages;
    }
    public long getWorldResetDelay() {
        return worldResetDelay;
    }
    public int getLoadRadius() {
        return loadRadius;
    }
    public long getLoadingDelay() {
        return loadingDelay;
    }

}
