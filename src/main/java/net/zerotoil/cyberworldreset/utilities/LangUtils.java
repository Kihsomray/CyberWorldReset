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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangUtils {

    private final CyberWorldReset main;

    private final ActionBar actionBar;
    private final Title title;

    private final Pattern HEX_PATTERN = Pattern.compile("\\{#([A-Fa-f0-9]{6})}");

    // fields needed for time formatter
    private String PLURAL_REGEX = "\\s*\\([^\\)]*\\)\\s*";
    private char START_DELIMITER = '(';
    private char END_DELIMITER = ')';

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
        if (player.isOp()) return true;
        for (PermissionAttachmentInfo permissionNode : player.getEffectivePermissions()) {
            if (permissionNode.getPermission().toLowerCase().startsWith(permission.toLowerCase())) return true;
        }
        return false;
    }

    public String formatPapiString(String placeholder, String world, List<String> placeholders, List<String> replacements) {
        placeholder.replace("{world}", world);
        if (placeholders != null)
            for (int i = 0; i < placeholders.size(); i++)
                placeholder = placeholder.replace("{" + placeholders.get(i) + "}", replacements.get(i));

        return placeholder;

    }

    public String formatTime(long seconds) {

        if (seconds <= 0) return getColor(checkPluralFormat(0, main.lang().getTimeSecondsFormat()), false);

        String formattedTime = "";
        long daysTotal, hoursTotal, minutesTotal;

        // gets day time
        daysTotal = getFixedTime(seconds, 86400);
        seconds = seconds - (daysTotal * 86400);
        if (daysTotal > 0) formattedTime += (checkPluralFormat(daysTotal, main.lang().getTimeDaysFormat()) + main.lang().getTimeSplitterFormat());

        // gets hour time
        hoursTotal = getFixedTime(seconds, 3600);
        seconds = seconds - (hoursTotal * 3600);
        if (hoursTotal > 0) formattedTime += checkPluralFormat(hoursTotal, main.lang().getTimeHoursFormat()) + main.lang().getTimeSplitterFormat();

        // gets minute time
        minutesTotal = getFixedTime(seconds, 60);
        seconds = seconds - (minutesTotal * 60);
        if (minutesTotal > 0) formattedTime += checkPluralFormat(minutesTotal, main.lang().getTimeMinutesFormat()) + main.lang().getTimeSplitterFormat();

        // gets second time
        if (seconds > 0) formattedTime += checkPluralFormat(seconds, main.lang().getTimeSecondsFormat()) + main.lang().getTimeSplitterFormat();

        // returns final string
        return getColor(formattedTime.substring(0, formattedTime.length() - main.lang().getTimeSplitterFormat().length()), false);

    }

    // gets proper time for a time format
    private long getFixedTime(long seconds, long formatter) {

        long tempSeconds = seconds % formatter;
        return (seconds - tempSeconds) / formatter;

    }

    // checks plural formatting and applies it
    private String checkPluralFormat(long value, String string) {

        string = string.replace("{time}", value + "");
        if (value == 1) return string.replaceAll(PLURAL_REGEX, "");
        else return string.replace(START_DELIMITER + "", "").replace(END_DELIMITER + "", "");

    }

}
