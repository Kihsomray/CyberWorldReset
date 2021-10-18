package net.zerotoil.cyberworldreset.handlers;

import net.zerotoil.cyberworldreset.interfaces.ActionBar;
import net.zerotoil.cyberworldreset.interfaces.Reflection;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class ActionBarLegacy implements ActionBar, Reflection {

    @Override
    public void send(Player player, String message) {

        try {
            Constructor<?> constructor = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent"), byte.class);
            Object icbc = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + message + "\"}");
            Object packet = constructor.newInstance(icbc, (byte) 2);
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
