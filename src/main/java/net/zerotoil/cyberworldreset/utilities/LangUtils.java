package net.zerotoil.cyberworldreset.utilities;

import net.md_5.bungee.api.ChatColor;
import net.zerotoil.cyberworldreset.CyberWorldReset;
import net.zerotoil.cyberworldreset.handlers.ActionBarLegacy;
import net.zerotoil.cyberworldreset.handlers.ActionBarObject;
import net.zerotoil.cyberworldreset.handlers.TitleLegacy;
import net.zerotoil.cyberworldreset.handlers.TitleObject;
import net.zerotoil.cyberworldreset.interfaces.ActionBar;
import net.zerotoil.cyberworldreset.interfaces.Title;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangUtils {

    private final CyberWorldReset main;

    private final ActionBar actionBar;
    private final Title title;

    private final Pattern HEX_PATTERN = Pattern.compile("\\{#([A-Fa-f0-9]{6})}");

    public LangUtils(CyberWorldReset main) {
        this.main = main;
        this.actionBar = main.getVersion() < 11 ? new ActionBarLegacy() : new ActionBarObject();
        this.title = main.getVersion() < 10 ? new TitleLegacy() : new TitleObject();
    }

    private String getHex(String msg) {
        String translation = msg;
        Matcher matcher = HEX_PATTERN.matcher(translation);
        while (matcher.find()) {
            String hexString = matcher.group();
            hexString = "#" + hexString.substring(2, hexString.length()-1);
            ChatColor hex = ChatColor.of(hexString);
            String before = translation.substring(0, matcher.start());
            String after = translation.substring(matcher.end());
            translation = before + hex + after;
            matcher = HEX_PATTERN.matcher(translation);
        }
        return ChatColor.translateAlternateColorCodes('&',translation);
    }

    private String oldColor(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    // gets color of message
    public String getColor(String msg, boolean addPrefix){
        msg = addPrefix ? main.lang().getPrefix() + msg : msg;
        return main.getVersion() < 16 ? oldColor(msg) : getHex(msg);
    }

    public void sendActionBar(Player player, String message) {
        message = getColor(message, false);
        actionBar.send(player, message);
    }

    public void sendTitle(Player player, String theTitle, String subtitle, List<Integer> times) {
        if (theTitle == null || theTitle.equals("")) return;
        title.send(player, theTitle, subtitle, times.get(0), times.get(1), times.get(2));
    }

    // does player have a permission?
    public boolean CheckPermMSG(Player player, String permKey) {
        if (player == null) return false;
        if (player.hasPermission("CyberWorldReset." + permKey)) return false;
        sendNoPermMSG(player, permKey);
        return true;
    }

    // player/admin help message
    public boolean sendHelpMSG(Player player) {

        if (player == null) return true;

        if (player.hasPermission("CyberWorldReset.admin.help")) {
            for (String i : main.lang().getAdminHelp()) player.sendMessage(getColor(i, false));
            return true;
        }

        if (player.hasPermission("CyberWorldReset.player.help")) {
            for (String i : main.lang().getPlayerHelp()) player.sendMessage(getColor(i, false));
            return true;
        }

        sendNoPermMSG(player, "player.help");
        return true;

    }

    // no permission message
    public void sendNoPermMSG(Player player, String permKey) {
        main.lang().getMsg("no-permission").send(player, true, new String[]{"permission", "player"}, new String[]{"CyberWorldReset." + permKey, player.getDisplayName()});
    }

    // gets language of config
    public String getLang(String english, String spanish, String russian) {

        if (main.config().getLang().equalsIgnoreCase("es")) return spanish;
        if (main.config().getLang().equalsIgnoreCase("ru")) return russian;
        return english;

    }

    // converts message to list
    public List<String> convertList(Configuration config, String path) {

        // if already list
        if (config.isList(path)) return config.getStringList(path);

        // if single string
        List<String> list = new ArrayList<>();
        if (!config.getString(path).contains("[Ljava.lang.String")) list.add(config.getString(path));
        return list;

    }

    public boolean hasParentPerm(Player player, String permission) {
        for (PermissionAttachmentInfo permissionNode : player.getEffectivePermissions())
            if (permissionNode.getPermission().startsWith(permission)) return true;
        return false;
    }

    public String formatPapiString(String placeholder, String world, List<String> placeholders, List<String> replacements) {
        placeholder.replace("{world}", world);
        for (int i = 0; i < placeholders.size(); i++) {
            placeholder = placeholder.replace("{" + placeholders.get(i) + "}", replacements.get(i));
        }
        return placeholder;

    }


    public String formatTime(long seconds) {

        String formattedTime = "";

        String daysString = main.lang().getTimeDaysFormat();
        String hoursString = main.lang().getTimeHoursFormat();
        String minutesString = main.lang().getTimeMinutesFormat();
        String secondsString = main.lang().getTimeSecondsFormat();
        String splitter = main.lang().getTimeSplitterFormat();

        if (seconds <= 0) return getColor(secondsString.replace("(", "").replace(")", "").replace("{time}", "0"), false);

        long daySeconds = seconds;
        if (seconds != 0) daySeconds = seconds % 86400;
        long days = (seconds - daySeconds) / 86400;

        long hourSeconds = daySeconds;
        if (daySeconds != 0) hourSeconds = daySeconds % 3600;
        long hours = (daySeconds - hourSeconds) / 3600;

        long minuteSeconds = hourSeconds;
        if (hourSeconds != 0) minuteSeconds = hourSeconds % 60;
        long minutes = (hourSeconds - minuteSeconds) / 60;

        // day formatting
        daysString = daysString.replace("{time}", days + "");
        if (days == 1) {
            daysString = daysString.replaceAll("\\s*\\([^\\)]*\\)\\s*", "");
        } else {
            daysString = daysString.replace("(", "").replace(")", "");
        }
        if (days != 0) {
            if ((hours == 0) && (minutes == 0) && (minuteSeconds == 0)) {
                formattedTime = daysString;
            } else {
                formattedTime = daysString + splitter;
            }
        }


        // hour formatting
        hoursString = hoursString.replace("{time}", hours + "");
        if (hours == 1) {
            hoursString = hoursString.replaceAll("\\s*\\([^\\)]*\\)\\s*", "");
        } else {
            hoursString = hoursString.replace("(", "").replace(")", "");
        }
        if (hours != 0) {
            if ((minutes == 0) && (minuteSeconds == 0)) {
                formattedTime = formattedTime + hoursString;
            } else {
                formattedTime = formattedTime + hoursString + splitter;
            }
        }


        // minute formatting
        minutesString = minutesString.replace("{time}", minutes + "");
        if (minutes == 1) {
            minutesString = minutesString.replaceAll("\\s*\\([^\\)]*\\)\\s*", "");
        } else {
            minutesString = minutesString.replace("(", "").replace(")", "");
        }
        if (minutes != 0) {
            if (minuteSeconds == 0) {
                formattedTime = formattedTime + minutesString;
            } else {
                formattedTime = formattedTime + minutesString + splitter;
            }
        }


        // second formatting
        secondsString = secondsString.replace("{time}", minuteSeconds + "");
        if (minuteSeconds == 1) {
            secondsString = secondsString.replaceAll("\\s*\\([^\\)]*\\)\\s*", "");
        } else {
            secondsString = secondsString.replace("(", "").replace(")", "");
        }
        if (minuteSeconds != 0) formattedTime = formattedTime + secondsString;

        return getColor(formattedTime, false);

    }

}
