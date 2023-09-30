package net.zerotoil.cyberworldreset.old.handlers;

import net.zerotoil.cyberworldreset.old.interfaces.Reflection;
import net.zerotoil.cyberworldreset.old.interfaces.Title;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class TitleLegacy implements Title, Reflection {

    private int in;
    private int stay;
    private int out;

    private void titleSubtitle(Player player, String message, boolean isTitle) {

        try {
            Object e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
            Object chatMessage = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + message + "\"}");
            Constructor<?> subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object titlePacket = subtitleConstructor.newInstance(e, chatMessage, in, stay, out);

            sendPacket(player, titlePacket);

            e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField((isTitle ? "" : "SUB") + "TITLE").get(null);
            chatMessage = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + message + "\"}");
            subtitleConstructor = isTitle ?
                    getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent")) :
                    getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            titlePacket = isTitle ? subtitleConstructor.newInstance(e, chatMessage) : subtitleConstructor.newInstance(e, chatMessage, in/20, stay/20, out/20);

            sendPacket(player, titlePacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(Player player, String title, String subtitle, int in, int stay, int out) {
        this.in = in;
        this.stay = stay;
        this.out = out;
        titleSubtitle(player, title, true);
        titleSubtitle(player, subtitle, false);
    }

}
