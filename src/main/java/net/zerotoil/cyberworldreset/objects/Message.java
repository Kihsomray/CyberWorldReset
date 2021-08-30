package net.zerotoil.cyberworldreset.objects;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;

public class Message {

    private CyberWorldReset main;

    private String message;
    private String path;

    public Message(CyberWorldReset main, String path) {
        this.main = main;
        this.path = path;
        message = "";
        if (main.files().getConfig("lang").isSet("messages." + path))
            message = ChatColor.translateAlternateColorCodes('&', main.files().getConfig("lang").getString("messages." + path));

    }

    public void send(Player player, boolean addPrefix, String[] placeholders, String[] replacements) {
        if (message.equalsIgnoreCase("")) return;
        String editedMessage = message;
        for (int i = 0; i < placeholders.length; i++) {
            editedMessage = editedMessage.replace("{" + placeholders[i] + "}", replacements[i]);
        }
        if (player == null) {
            Bukkit.getLogger().info(editedMessage);
            return;
        }
        if (addPrefix) editedMessage = main.lang().getPrefix() + editedMessage;
        player.sendMessage(editedMessage);
    }

    public String toString(boolean addPrefix) {
        if (message.equalsIgnoreCase("")) return null;
        if (addPrefix) return main.lang().getPrefix() + message;
        return message;
    }

}
