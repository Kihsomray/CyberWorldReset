package net.zerotoil.cyberworldreset.cache;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import net.zerotoil.cyberworldreset.objects.Message;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Lang {

    private CyberWorldReset main;
    private Configuration config;
    private boolean updateLang;

    private String prefix;

    private List<String> playerHelp = new ArrayList<>();
    private List<String> adminHelp = new ArrayList<>();

    private HashMap<String, Message> messages = new HashMap<>();

    private String timeDaysFormat;
    private String timeHoursFormat;
    private String timeMinutesFormat;
    private String timeSecondsFormat;
    private String timeSplitterFormat;

    public Lang(CyberWorldReset main) {

        this.main = main;
        config = main.files().getConfig("lang");
        updateLang = main.config().isUpdateLang();
        if (updateLang) main.files().get("lang").updateConfig();
        prefix = "";
        if (!config.getString("messages.prefix").equalsIgnoreCase("")) prefix = getColor(config.getString("messages.prefix") + " ");

    }

    // sets up all the lang strings
    public void loadLang() {

        playerHelp = getHelpList("help-player");
        adminHelp = getHelpList("admin-help");

        if (!messages.isEmpty()) messages.clear();
        for (String i : config.getConfigurationSection("messages").getKeys(false)) {
            if (i.matches("(?i)prefix|help-player|help-admin")) continue;
            messages.put(i, new Message(main, i));
        }

        timeDaysFormat = getTime("days", "&a{time} Day(s)");
        timeHoursFormat = getTime("hours", "&a{time} Hour(s)");
        timeMinutesFormat = getTime("minutes", "&a{time} Minute(s)");
        timeSecondsFormat = getTime("seconds", "&a{time} Second(s)");
        timeSplitterFormat = getTime("splitter", "&a, ");

    }

    private List<String> getHelpList(String path) {
        if (config.isSet("messages." + path)) return config.getStringList("config." + path);
        return Collections.emptyList();
    }

    private String getTime(String path, String defaultValue) {
        if (config.isSet("time." + path)) return getColor(config.getString("time." + path));
        return getColor(defaultValue);
    }

    // private
    private String getColor(String message) {
        return main.langUtils().getColor(message,false);
    }

    // public
    public boolean isUpdateLang() {
        return updateLang;
    }
    public String getPrefix() {
        return prefix;
    }
    public Configuration getConfig() {
        return config;
    }
    public List<String> getPlayerHelp() {
        return playerHelp;
    }
    public List<String> getAdminHelp() {
        return adminHelp;
    }
    public HashMap<String, Message> getMessages() {
        return messages;
    }
    public String getTimeDaysFormat() {
        return timeDaysFormat;
    }
    public String getTimeHoursFormat() {
        return timeHoursFormat;
    }
    public String getTimeMinutesFormat() {
        return timeMinutesFormat;
    }
    public String getTimeSecondsFormat() {
        return timeSecondsFormat;
    }
    public String getTimeSplitterFormat() {
        return timeSplitterFormat;
    }

    public Message getMsg(String msgKey) {
        return getMessages().get(msgKey);
    }

}