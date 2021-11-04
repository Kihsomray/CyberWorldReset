package net.zerotoil.cyberworldreset.utilities;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class WorldUtils {

    private CyberWorldReset main;

    public WorldUtils(CyberWorldReset main) {
        this.main = main;
    }

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
        return new Location(Bukkit.getWorld(worldName), location[0], location[1], location[2], 0, 0);
    }

    public double[] coordinateStringToDouble(String locationString) {
        locationString = locationString.replace("[", "").replace("]", "");
        return Arrays.stream(locationString.split(", ")).mapToDouble(Double::parseDouble).toArray();
    }

    public boolean areCoordinates(String string) {
        try {
            double[] coords = coordinateStringToDouble(string);
            if (coords.length != 3) return false;
        } catch (Exception e) {
            return false;
        }
        return true;

    }


}
