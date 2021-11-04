package net.zerotoil.cyberworldreset.addons;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.zerotoil.cyberworldreset.CyberWorldReset;
import net.zerotoil.cyberworldreset.objects.Lag;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.text.DecimalFormat;
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
        if (identifier.contains("reset_status_")) {
            String world = identifier.substring(13); // replace with world papi
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

        return null;

    }

}
