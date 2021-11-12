package net.zerotoil.cyberworldreset.addons;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.zerotoil.cyberworldreset.CyberWorldReset;
import net.zerotoil.cyberworldreset.objects.Lag;
import net.zerotoil.cyberworldreset.objects.WorldObject;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlaceholderAPI extends PlaceholderExpansion {

    private CyberWorldReset main;
    private String zeroSec;

    public PlaceholderAPI(CyberWorldReset main) {

        this.main = main;
        zeroSec = ChatColor.stripColor(main.langUtils().formatTime(0));

    }

    @Override
    public String getAuthor() {
        return main.getDescription().getAuthors().toString().replace("[", "").replace("]", "'");
    }

    @Override
    public String getIdentifier() {
        return "cwr";
    }

    @Override
    public String getVersion() {
        return main.getDescription().getVersion();
    }

    @Override // required
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (identifier.toLowerCase().startsWith("reset_status_")) {
            String world = identifier.substring(13); // replace with world papi
            if (!main.worlds().getWorlds().containsKey(world)) return main.langUtils().formatPapiString(main.lang().getPapiInvalidWorld(), world, new ArrayList<>(), new ArrayList<>());
            return getWorldStatus(world);
        }

        else if (identifier.equalsIgnoreCase("reset_status")) {

            if (main.worlds().getWorlds().isEmpty()) return main.langUtils().formatPapiString(main.lang().getPapiInvalidWorld(), "null", new ArrayList<>(), new ArrayList<>());
            long currentTime = System.currentTimeMillis();
            String soonestWorld = new ArrayList<>(main.worlds().getWorlds().values()).get(0).getWorldName();

            for (WorldObject worldObject : main.worlds().getWorlds().values()) {

                long time = worldObject.getTimeUntilReset();
                if ((time < currentTime) && (time > 0)) {
                    currentTime = time;
                    soonestWorld = worldObject.getWorldName();
                }
                if (worldObject.isResetting()) return getWorldStatus(worldObject.getWorldName());
            }
            return getWorldStatus(soonestWorld);

        }
        if (identifier.equalsIgnoreCase("tps")) {
            String tps = Lag.getLowerTPS() + "0";
            if (tps.length() > 5) tps = tps.substring(0, 5);
            return tps;
        }
        int loadRadius = main.config().getLoadRadius();
        if (identifier.equalsIgnoreCase("chunk_radius")) {
            return loadRadius + "";
        }
        if (identifier.equalsIgnoreCase("chunk_diameter")) {
            return (loadRadius * 2 + 1) + "";
        }
        if (identifier.equalsIgnoreCase("chunk_area")) {
            return ((loadRadius * 2 + 1) * (loadRadius * 2 + 1)) + "";
        }
        if (identifier.startsWith("remaining_time_") || identifier.startsWith("remaining_seconds_")) {
            String world = identifier.substring(15); // replace with world papi
            if (identifier.startsWith("remaining_s")) world = identifier.substring(18);
            List<String> eL = Collections.emptyList();
            if (!main.worlds().getWorlds().containsKey(world)) return main.langUtils().formatPapiString(main.lang().getPapiInvalidWorld(), world, eL, eL);
            long timeLeft = main.worlds().getWorld(world).getTimeUntilReset();
            if (timeLeft == 0) return main.langUtils().formatPapiString(main.lang().getPapiNoTimers(), world, eL, eL);
            if (identifier.startsWith("remaining_t")) return ChatColor.stripColor(main.langUtils().formatTime(timeLeft));
            else return timeLeft + "";
        }

        if (identifier.startsWith("loaded_chunks_") || identifier.startsWith("remaining_chunks_")) {
            String world = identifier.substring(14); // replace with world papi
            if (identifier.startsWith("r")) world = identifier.substring(17);
            List<String> eL = Collections.emptyList();
            if (!main.worlds().getWorlds().containsKey(world)) return main.langUtils().formatPapiString(main.lang().getPapiInvalidWorld(), world, eL, eL);
            long chunksLeft = main.worlds().getWorld(world).getChunkCounter();
            long area = ((loadRadius * 2L + 1) * (loadRadius * 2L + 1));
            if (identifier.startsWith("r")) {
                if (chunksLeft == -2) return area + "";
                if (chunksLeft == -1) return "0";
                return Math.max(area - chunksLeft, 0) + "";
            }
            if (chunksLeft == -2) return "0";
            if (chunksLeft == -1) return area + "";
            return chunksLeft + "";
        }

        if (identifier.startsWith("reset_percent_")) {
            String world = identifier.substring(14); // replace with world papi
            List<String> eL = Collections.emptyList();
            if (!main.worlds().getWorlds().containsKey(world)) return main.langUtils().formatPapiString(main.lang().getPapiInvalidWorld(), world, eL, eL);
            return main.worlds().getWorld(world).getPercentRemaining() + "";
        }

        return null;

    }

    private String getWorldStatus(String world) {

        List<String> eL = Collections.emptyList();
        if (!main.worlds().getWorlds().containsKey(world)) return main.langUtils().formatPapiString(main.lang().getPapiInvalidWorld(), world, eL, eL);
        if (main.worlds().getWorld(world).isResetting()) {
            if (main.worlds().getWorld(world).getChunkCounter() == -2) return main.langUtils().formatPapiString(main.lang().getPapiInitializing(), world, eL, eL);
            if (main.worlds().getWorld(world).getChunkCounter() == -1) return main.langUtils().formatPapiString(main.lang().getPapiFinishing(), world, eL, eL);
            if (main.worlds().getWorld(world).isStartingReset()) return main.langUtils().formatPapiString(main.lang().getPapiStarting(), world, eL, eL);
            long seconds = main.worlds().getWorld(world).getTimeRemaining();
            String formatted = ChatColor.stripColor(main.langUtils().formatTime(seconds));
            long percent = main.worlds().getWorld(world).getPercentRemaining();
            String tps = Lag.getLowerTPS() + "0";
            if (tps.length() > 5) tps = tps.substring(0, 5);
            long width = main.config().getLoadRadius() * 2L + 1;
            return main.langUtils().formatPapiString(main.lang().getPapiProgress(), world, Arrays.asList("seconds", "formattedTime", "percent", "tps", "chunkNumber", "chunkTotal"),
                    Arrays.asList(seconds + "", formatted, percent + "", tps, main.worlds().getWorld(world).getChunkCounter() + "", (width * width) + ""));
        }
        if (main.worlds().getWorld(world).getTimedResets().isEmpty()) return main.langUtils().formatPapiString(main.lang().getPapiNoTimers(), world, eL, eL);
        long timeLeft = main.worlds().getWorld(world).getTimeUntilReset();
        if (timeLeft == 0) return main.langUtils().formatPapiString(main.lang().getPapiNoTimers(), world, eL, eL);
        return main.langUtils().formatPapiString(main.lang().getPapiCountdown(), world, Arrays.asList("seconds", "formattedTime"),
                Arrays.asList(timeLeft + "", ChatColor.stripColor(main.langUtils().formatTime(timeLeft))));

    }

}
