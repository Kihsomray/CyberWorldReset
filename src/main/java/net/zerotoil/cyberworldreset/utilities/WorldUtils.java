package net.zerotoil.cyberworldreset.utilities;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class WorldUtils {

    public String getLevelName() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("server.properties"));
            return props.getProperty("level-name");
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean isWorld(String worldName) {
        return Bukkit.getWorld(worldName) != null;
    }

    public Location getLocationFromString(String worldName, String locationString) {
        double[] location = coordinateStringToDouble(locationString);
        float pitch = 0, yaw = 0;
        if (location.length >= 4) pitch = (float) location[3];
        if (location.length == 5) yaw = (float) location[4];
        return new Location(Bukkit.getWorld(worldName), location[0], location[1], location[2], yaw, pitch);
    }

    public double[] coordinateStringToDouble(String locationString) {
        locationString = locationString.replace("[", "").replace("]", "");
        return Arrays.stream(locationString.split(", ")).mapToDouble(Double::parseDouble).toArray();
    }

    public boolean areCoordinates(String string) {
        try {
            double[] doubles = coordinateStringToDouble(string);
            if (doubles.length < 3 || doubles.length > 5) return false;
        }
        catch (Exception e) { return false; }
        return true;
    }
}
