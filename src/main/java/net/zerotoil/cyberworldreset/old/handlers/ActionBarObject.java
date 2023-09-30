package net.zerotoil.cyberworldreset.old.handlers;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.zerotoil.cyberworldreset.old.interfaces.ActionBar;
import org.bukkit.entity.Player;

public class ActionBarObject implements ActionBar {

    @Override
    public void send(Player player, String message) {
       player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

}
