package net.zerotoil.cyberworldreset.objects;

import com.Zrips.CMI.CMI;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiversePlugin;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.boss.DragonBattle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;

public class WorldObject {

    private final CyberWorldReset main; // req
    private boolean enabled; // req
    private boolean lastSaved;
    private String worldName; // req
    private World world; // req, based off of worldName
    private List<String> time; // not req
    private List<String> message; // not req
    private Long seed; // not req, use default
    private boolean randomSeed;
    private String defaultGamemode; // not req, use default
    private World.Environment environment; // not req, use default
    private String generator;
    private List<String> commands = new ArrayList<>(); // not req
    private List<String> initialCommands = new ArrayList<>();
    private boolean resetting;

    // safe world module - not req
    private boolean safeWorldEnabled; // boolean defaults to false
    private String safeWorld;
    private long safeWorldDelay;
    private String safeWorldSpawn; // check Location object to do so

    // warning module - not req
    private boolean warningEnabled; // boolean defaults to false
    private List<String> warningMessage = new ArrayList<>();
    private ArrayList<Long> warningTime = new ArrayList<>();
    private String warningTitle;
    private String warningSubtitle;
    private List<Integer> warningTitleFade;

    // timed resets module
    private final HashMap<String, TimedReset> timedResets = new HashMap<>();

    // teleport player module
    private final  List<Player> tpPlayers = new ArrayList<>();

    // chunk loading cache
    private final long loadDelay;
    private ArrayList<Integer> chunkInfo;
    private int xChunk;
    private int zChunk;
    private int chunkNumber;
    private int chunkCounter;
    private Map<Long, List<Integer>> chunks;

    private boolean startingReset;

    private final CommandSender console = Bukkit.getConsoleSender();

    public WorldObject(CyberWorldReset main, String worldName) {

        this.main = main;
        enabled = false;
        lastSaved = false;
        this.worldName = worldName;
        world = Bukkit.getWorld(worldName);
        time = null;
        message = null;
        assert world != null;
        seed = world.getSeed();
        environment = world.getEnvironment();
        safeWorldEnabled = false;
        randomSeed = false;
        warningEnabled = false;
        warningMessage.add("Warning: resetting the world {world} in {time}.");
        warningTime.add(10L);

        warningTitle = null;
        warningSubtitle = null;
        warningTitleFade = Arrays.asList(20, 60, 20);

        message = new ArrayList<>();
        message.add("");
        loadDelay = main.config().getLoadingDelay();
        resetting = false;
        safeWorldSpawn = "DEFAULT";
        chunkCounter = -2;
        startingReset = false;

        try {
            generator = world.getGenerator().toString();
        } catch (Exception e) {
            generator = null;
        }

    }

    public boolean regenWorld(Player sender) {

        if (resetting) {
            main.lang().getMsg("already-resetting").send(sender, true, new String[]{"world"}, new String[]{worldName});
            return true;
        }

        if (!enabled) {
            main.lang().getMsg("not-enabled").send(sender, true, new String[]{"world"}, new String[]{worldName});
            return false;
        }

        if (lastSaved && (main.zipUtils().getLastModified(worldName) == null)) {
            main.lang().getMsg("no-saves").send(sender, true, new String[]{"world"}, new String[]{worldName});
            return false;
        }

        if (safeWorldEnabled && (safeWorld.equals(worldName) || Bukkit.getWorld(safeWorld) == null)) {
            main.lang().getMsg("invalid-safeworld").send(sender, true, new String[]{"world", "safeWorld"}, new String[]{worldName, safeWorld});
            return false;
        }

        if (safeWorldEnabled && (safeWorld.equals(worldName) || Bukkit.getWorld(safeWorld) == null)) {
            main.lang().getMsg("invalid-safeworld").send(sender, true, new String[]{"world", "safeWorld"}, new String[]{worldName, safeWorld});
            return false;
        }

        resetting = true;
        sendCommands(true);
        tpPlayersAway();

        // default world check
        if (Objects.equals(main.worldUtils().getLevelName(), worldName)) return regenFail("default-world-fail", sender);

        // can the world unload?
        if (!Bukkit.unloadWorld(worldName, false)) return regenFail("unload-failed", sender);

        // save before reset
        if (main.config().isSaveWorldBeforeReset() && !saveWorld(null, false)) regenFail(null, null);

        // deletes old world files
        try {
            FileUtils.deleteDirectory(new File(main.getDataFolder().getParentFile().getParentFile(), worldName));
        } catch (Exception e) {
            return regenFail("file-delete-failed", sender);
        }

        return regen2(sender);
    }

    // second process in the reset sequence
    private boolean regen2(Player sender) {

        // reset delay
        long resetDelay = main.config().getWorldResetDelay();
        if (main.config().getWorldResetDelay() <= 0) resetDelay = 1;

        (new BukkitRunnable() {

            @Override
            public void run() {

                // should spawn chunks load?
                // creates world
                WorldCreator finalWorld = new WorldCreator(worldName);
                finalWorld.environment(environment);

                if (!lastSaved) {
                    if (randomSeed) seed = new Random().nextLong();
                    finalWorld.seed(seed);
                } else {
                    if (!rollbackWorld(sender)) {
                        regenFail(null, sender);
                        return;
                    }
                }

                // generator settings
                if (generator != null) {
                    try {
                        finalWorld.generator(generator);
                    } catch (Exception e) {
                        main.logger("&cFailed to set the generator " + generator + ". Please check the name. Using default generator.");
                    }
                }
                finalWorld.createWorld();

                if (main.isMultiverseEnabled()) {
                    try {
                        main.multiverse().getMVWorldManager().getMVWorld(getWorld()).setKeepSpawnInMemory(main.config().getLoadingType().matches("(?i)STANDARD"));
                    } catch (Exception e) {
                        main.logger("&cFailed to prevent Multiverse from loading spawn chunks. Please check your generator name.");
                    }
                }

                // ultra fast chunk loading
                if (main.config().getLoadingType().matches("(?i)ULTRA-FAST")) getWorld().loadChunk(getWorld().getSpawnLocation().getChunk());

                // fast, normal, safe, ultra-safe chunk loading
                if (main.config().getLoadingType().matches("(?i)FAST|NORMAL|SAFE|ULTRA-SAFE"))
                    newProperLoading(sender);
                    // else safeLoadChunks(main.config().getLoadRadius(), sender);
                else finishRegen(sender);
            }
        }).runTaskLater(main, resetDelay);
        return true;
    }

    private void tpPlayersAway() {
        // kicks or teleports player
        if (Objects.isNull(safeWorld) || safeWorld.equals(worldName) || (!safeWorldEnabled)) {
            main.onJoin().setServerOpen(false);
            if (!Bukkit.getOnlinePlayers().isEmpty())
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().getName().equalsIgnoreCase(worldName)) continue;
                    player.kickPlayer(main.lang().getMsg("kick-reason").toString(false).replace("{world}", worldName));
                }
        }
        else {
            if (getWorld().getEnvironment().toString().contains("THE_END")) {
                try {
                    DragonBattle dBattle = getWorld().getEnderDragonBattle();
                    if (dBattle != null) dBattle.getBossBar().removeAll();
                } catch (Exception e) {
                    // for versions lower than 1.13.
                }
            }

            if (!tpPlayers.isEmpty()) tpPlayers.clear();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().getName().equalsIgnoreCase(worldName)) continue;

                if (player.isDead()) player.kickPlayer(main.lang().getMsg("kick-reason").toString(false).replace("{world}", worldName));

                tpPlayers.add(player);
                main.lang().getMsg("teleporting-safe-world").send(player, true,
                        new String[]{"world", "safeWorld"}, new String[]{worldName, safeWorld});

                if (!safeWorldSpawn.equalsIgnoreCase("default"))
                    player.teleport(main.worldUtils().getLocationFromString(safeWorld, safeWorldSpawn));
                else player.teleport(Bukkit.getWorld(safeWorld).getSpawnLocation());

                main.lang().getMsg("teleported-safe-world").send(player, true,
                        new String[]{"world", "safeWorld"}, new String[]{worldName, safeWorld});
            }
            main.onWorldChange().addClosedWorld(worldName);
        }
    }

    private boolean regenFail(String msgKey, Player player) {
        if (msgKey != null) main.lang().getMsg(msgKey).send(player, true, new String[]{"world"}, new String[]{worldName});
        main.onWorldChange().removeClosedWorld(worldName);
        main.onJoin().setServerOpen(true);
        resetting = false;
        return false;
    }

    private void newProperLoading(Player sender) {
        int radius = main.config().getLoadRadius();
        int width = radius * 2 + 1;
        int area = width * width;
        Random random = new Random();
        chunkInfo = new ArrayList<>();

        int avgMsgInterval = (int) Math.round((random.nextInt(4) + 3) * (20.0 / loadDelay));
        long numMsg = (area - (area % avgMsgInterval)) / avgMsgInterval;
        for (int i = 0; i < numMsg; i++) {
            int test = (random.nextInt(avgMsgInterval) + avgMsgInterval * i);
            chunkInfo.add(test);
        }
        xChunk = 0;
        zChunk = 0;
        chunkNumber = 0;
        chunkCounter = 1;
        Bukkit.getLogger().info("The world is being loaded, please wait!");
        chunks = new HashMap<>();
        getWorld().loadChunk(getWorld().getSpawnLocation().getChunk());
        startingReset = true;
        (new BukkitRunnable() {

            @Override
            public void run() {
                printChunkInfo(width);

                int z = 1;
                chunks.put((long) z, Arrays.asList(xChunk, zChunk));
                for (int i = 1; i <= radius; i++) {
                    xChunk++;
                    z++;
                    chunks.put((long) z, Arrays.asList(xChunk, zChunk));
                    for (int a = 1; a <= ((i * 2) - 1); a++) {
                        zChunk--;
                        z++;
                        chunks.put((long) z, Arrays.asList(xChunk, zChunk));
                    }
                    int w = i * 2;
                    for (int a = 1; a <= 3; a++) {
                        for (int b = w; b > 0; b--) {
                            if (a == 1) xChunk--;
                            else if (a == 2) zChunk++;
                            else xChunk++;
                            z++;
                            chunks.put((long) z, Arrays.asList(xChunk, zChunk));
                        }
                    }
                }
                startingReset = false;
                newChunkLoading(sender);

            }


        }).runTaskLater(main, 200L);

    }

    private void newChunkLoading(Player sender) {
        int width = main.config().getLoadRadius() * 2 + 1;

        int spawnX = getWorld().getSpawnLocation().getChunk().getX(), spawnZ = getWorld().getSpawnLocation().getChunk().getZ();

        int counter = chunkCounter + 3;
        for (int i = chunkCounter; i < counter; i++) {
            if (chunkNumber >= width * width) {

                printChunkInfo(width);
                finishRegen(sender);
                return;

            }
            chunkNumber++;
            if (chunkInfo.contains(chunkNumber)) printChunkInfo(width);
            //main.logger(chunks.get((long) chunkCounter).get(0) + spawnX + " " + (chunks.get((long) chunkCounter).get(1) + spawnZ));
            xChunk = chunks.get((long) chunkCounter).get(0) + spawnX;
            zChunk = chunks.get((long) chunkCounter).get(1) + spawnZ;
            //main.logger("pre: " + main.multiverse().getMVWorldManager().getMVWorld(getWorld()).isKeepingSpawnInMemory());
            if (main.getVersion() > 14) getWorld().getChunkAt(xChunk, zChunk).addPluginChunkTicket(main);
            else getWorld().loadChunk(xChunk, zChunk);
            //if (chunkCounter == 1) getWorld().setSpawnLocation(0, getWorld().getHighestBlockYAt(0, 0), 0);
            chunkCounter++;
        }

        (new BukkitRunnable() {

            @Override
            public void run() {

                newChunkLoading(sender);

            }

        }).runTaskLater(main, loadDelay * Math.round(20/Lag.getLowerTPS()));

    }

    private void printChunkInfo(int width) {
        final double tps = Lag.getNewTPS();
        String loading = "Loading";
        if (main.config().getLang().equalsIgnoreCase("es")) loading = "Cargando";
        else if (main.config().getLang().equalsIgnoreCase("ru")) loading = "Загрузка";
        if (main.config().isDetailedMessages() && main.getVersion() > 12)
            System.out.printf(loading + " [%s]: %3d%% | Chunk: %5d/%d | ETA: %-10s | TPS %.2f%n", worldName, Math.round((chunkNumber + 0.0) / (width * width) * 100), chunkNumber,
                    width * width, ChatColor.stripColor(main.langUtils().formatTime(Math.round((((width * width) - chunkNumber) * (loadDelay / 20.0) * (20.0 / tps)) / 3))), tps);
        else main.logger(loading + " [" + worldName + "]: " + Math.round((chunkNumber + 0.0) / (width * width) * 100) + "%");
    }

    public long getTimeRemaining() {
        final int width = main.config().getLoadRadius() * 2 + 1;
        final double tps = Lag.getNewTPS();
        return Math.round((((width * width) - chunkNumber) * (loadDelay / 20.0) * (20.0 / tps)) / 3.0);
    }

    public long getTimeUntilReset() {
        if (!enabled) return 0;
        if (isResetting()) return 0;
        long currentTime = Math.round(System.currentTimeMillis() / 1000L);
        long time = currentTime;
        for (TimedReset t : timedResets.values()) {
            if ((t.timeToReset() < time) && !(t.timeToReset() < 1)) time = t.timeToReset();
        }
        if (currentTime == time) return 0;
        return time;
    }

    public long getPercentRemaining() {
        final int width = main.config().getLoadRadius() * 2 + 1;
        return Math.max(Math.round((chunkNumber + 0.0) / (width * width) * 100), 0);
    }

    private void tpPlayersBack() {
        if (!safeWorldEnabled) return;

        Location spawnPoint = getWorld().getSpawnLocation();
        main.onDamage().setEnabled(true);

        for (Player player : tpPlayers) {
            if (!player.isOnline()) continue;
            main.lang().getMsg("teleporting-back").send(player, true, new String[]{"world", "safeWorld"}, new String[]{worldName, safeWorld});

            player.teleport(spawnPoint);
            main.lang().getMsg("teleported-back").send(player, true, new String[]{"world", "safeWorld"}, new String[]{worldName, safeWorld});
        }

        tpPlayers.clear();

        // disables onDamage 1.8 fixer
        (new BukkitRunnable() {

            @Override
            public void run() {
                main.onDamage().setEnabled(false);
            }

        }).runTaskLater(main, 100L);

    }

    private void finishRegen(Player sender) {

        chunkCounter = -1;

        try {
            if (main.isMultiverseEnabled())
                main.multiverse().getMVWorldManager().getMVWorld(getWorld()).setSpawnLocation(getWorld().getSpawnLocation());
        } catch (Exception e) {
            main.logger("&cFailed to set spawn location for " + worldName + " in Multiverse.");
        }

        deleteWGRegions();

        // sends successful regen to all players
        if ((message.size() != 1) || !message.get(0).equalsIgnoreCase(""))
            for (Player player : Bukkit.getOnlinePlayers())
                for (String i : message) player.sendMessage(main.langUtils().getColor(i.replace("{world}", worldName), true));

        sendCommands(false);

        main.onWorldChange().removeClosedWorld(worldName);
        if (!main.onJoin().isServerOpen()) main.onJoin().setServerOpen(true);
        if (safeWorldDelay == -1) {
            // nothing
        } else {
            (new BukkitRunnable() {
                @Override
                public void run() {
                    tpPlayersBack();
                }
            }).runTaskLater(main, 20L * (safeWorldDelay));
        }

        getWorld().setAutoSave(true);
        getWorld().setKeepSpawnInMemory(true);
        resetting = false;
        chunkCounter = -2;

        // refresh CMI stuff
        if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            if (main.config().isCmiWarpSave()) CMI.getInstance().getWarpManager().load();
            if (main.config().isCmiPortalRefresh()) CMI.getInstance().getPortalManager().load();
        }

        // refresh MV portals
        if (main.config().isMvPortalRefresh()) {

            MultiversePortals portals = (MultiversePortals) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Portals");
            if (portals != null) portals.reloadConfigs();

            MultiverseNetherPortals netherportals = (MultiverseNetherPortals) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-NetherPortals");
            if (netherportals != null) netherportals.loadConfig();

        }

        main.lang().getMsg("regen-success").send(sender, true, new String[]{"world"}, new String[]{worldName});
        chunkNumber = 0;
    }

    public void deleteWGRegions() {
        if (main.getVersion() < 13) return;

        if (main.config().isWorldGuardDelete() && main.worldGuard() != null) {

            RegionContainer regionContainer = main.worldGuard().getPlatform().getRegionContainer();
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(getWorld());

            try {
                for (ProtectedRegion region : regionContainer.get(weWorld).getRegions().values())
                    regionContainer.get(weWorld).removeRegion(region.getId());
            } catch (Exception e) {
                main.logger("&cSomething went wrong deleting WorldGuard regions.");
            }

        }
    }

    public boolean saveWorld(Player player, boolean saveWorld) {

        main.lang().getMsg("saving-world").send(player, true, new String[]{"world"}, new String[]{getWorldName()});
        File savedWorlds = new File(main.getDataFolder(),"saved_worlds");
        if (!savedWorlds.exists()) savedWorlds.mkdirs();
        if (saveWorld) {
            getWorld().save();
            (new BukkitRunnable() {
                @Override
                public void run() {
                    zipSavedWorld(player);
                }
            }).runTaskLater(main, 20L * Math.round(20 / Lag.getTPS()));
            return true;
        }
        else return zipSavedWorld(player);
    }

    private boolean zipSavedWorld(Player player) {
        try {
            main.zipUtils().zip(worldName);
            main.lang().getMsg("save-success").send(player, true, new String[]{"world"}, new String[]{worldName});
            return true;
        } catch (Exception e) {
            main.lang().getMsg("save-failed").send(player, true, new String[]{"world"}, new String[]{worldName});
            e.printStackTrace();
            return false;
        }
    }

    public boolean rollbackWorld(Player player){

        main.lang().getMsg("rolling-back-world").send(player, true, new String[]{"world"}, new String[]{worldName});
        File worldSave = main.zipUtils().getLastModified(worldName);

        if (worldSave == null) {
            main.lang().getMsg("rollback-failed").send(player, true, new String[]{"world"}, new String[]{worldName});
            return false;
        }

        try {
            main.zipUtils().unZip(worldSave);
            main.lang().getMsg("rollback-success").send(player, true, new String[]{"world"}, new String[]{worldName});
            return true;
        } catch (IOException e) {
            main.lang().getMsg("rollback-failed").send(player, true, new String[]{"world"}, new String[]{worldName});
            e.printStackTrace();
            return false;
        }
    }

    public void loadTimedResets() {
        if (!enabled) return;
        if (time.isEmpty()) return;
        for (String i : time) timedResets.put(i, new TimedReset(main, worldName, i, warningTime));
    }

    public void sendWarning(String unformatted) {

        String time = main.langUtils().formatTime(timedResets.get(unformatted).timeToReset());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != getWorld()) continue;
            for (String i : warningMessage)
                player.sendMessage(main.langUtils().getColor(i.replace("{world}", worldName).replace("{time}", time), true));

            // 63 --> 1 minute 3 seconds
            if (warningTitle == null) continue;
            String title = main.langUtils().getColor(warningTitle.replace("{world}", worldName).replace("{time}", time), false);
            String subtitle = main.langUtils().getColor(warningSubtitle.replace("{world}", worldName).replace("{time}", time), false);
            main.langUtils().sendTitle(player, title, subtitle, warningTitleFade);
        }
    }

    private void sendCommands(boolean initial) {

        String iS = "";
        List<String> commands = this.commands;
        int subStringInt = -8;
        if (initial) {
            iS = "initial:";
            commands = initialCommands;
            subStringInt = 0;
        }

        for (String cmd : commands) {

            // TODO redo this entire section, may be faulty

            while (cmd.charAt(0) == ' ') cmd = cmd.substring(1);
            if (cmd.startsWith("[initial") && !initial) continue;

            if (!cmd.startsWith("[")) {
                Bukkit.dispatchCommand(console, cmd.replace("{world}", worldName));
                continue;
            }

            if (cmd.toLowerCase().startsWith("[general]")) {
                cmd = cmd.substring(9);
                while (cmd.charAt(0) == ' ') cmd = cmd.substring(1);
                Bukkit.dispatchCommand(console, cmd.replace("{world}", worldName));
                continue;
            }

            if (cmd.toLowerCase().startsWith("[" + iS + "all-players]")) {
                cmd = cmd.substring(21 + subStringInt);
                while (cmd.charAt(0) == ' ') cmd = cmd.substring(1);
                final String newCmd = cmd;
                Bukkit.getOnlinePlayers().forEach(player -> Bukkit.dispatchCommand(console, replaceInCmd(newCmd, player)));
                continue;
            }

            if (cmd.toLowerCase().startsWith("[" + iS + "world-players]")) {
                cmd = cmd.substring(23 + subStringInt);
                while (cmd.charAt(0) == ' ') cmd = cmd.substring(1);
                final String newCmd = cmd;
                List<Player> players = tpPlayers;
                if (!resetting) players = getWorld().getPlayers();
                for (Player player : players) {
                    if (!player.isOnline()) continue;
                    Bukkit.dispatchCommand(console, replaceInCmd(newCmd, player));
                }
                continue;
            }

            if (cmd.toLowerCase().startsWith("[" + iS + "world-players:")) {
                cmd = cmd.substring(23 + subStringInt);
                World world = Bukkit.getWorld(cmd.substring(0, cmd.indexOf("]")));
                if (world == null) continue;
                cmd = cmd.substring(cmd.indexOf("]") + 1);
                while (cmd.charAt(0) == ' ') cmd = cmd.substring(1);
                final String newCmd = cmd;
                world.getPlayers().forEach(player -> Bukkit.dispatchCommand(console, replaceInCmd(newCmd, player)));
            }

        }
    }

    private String replaceInCmd(String cmd, Player player) {
        cmd = cmd.replace("{world}", worldName);
        if (player != null) {
            String[] placeholders = {"{playerName}", "{player}", "{playerDisplayName}", "{playerUUID}"};
            String[] values = {player.getName(), player.getName(), player.getDisplayName(), player.getUniqueId().toString()};
            cmd = StringUtils.replaceEach(cmd, placeholders, values);
        }
        return cmd;
    }

    public void cancelTimers() {
        if (timedResets.isEmpty()) return;
        for (TimedReset timedReset : timedResets.values()) timedReset.cancelAllTimers();
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }
    public boolean isEnabled() {
        return enabled;
    }
    public String getWorldName() {
        return worldName;
    }
    public List<String> getTime() {
        return time;
    }
    public List<String> getMessage() {
        return message;
    }
    public long getSeed() {
        return seed;
    }
    public String getGenerator() {
        if (generator == null) return "DEFAULT";
        if (generator.contains("BukkitChunkGeneratorWrapper@")) return generator.substring(0, generator.length() - 36);
        return generator;
    }
    public String getEnvironment() {
        return environment.toString();
    }

    public String getDefaultGamemode() {
        return defaultGamemode;
    }
    public List<String> getCommands() {
        return commands;
    }
    public boolean isSafeWorldEnabled() {
        return safeWorldEnabled;
    }
    public String getSafeWorld() {
        return safeWorld;
    }
    public long getSafeWorldDelay() {
        return safeWorldDelay;
    }
    public String getSafeWorldSpawn() {
        return safeWorldSpawn;
    }
    public boolean isWarningEnabled() {
        return warningEnabled;
    }
    public List<String> getWarningMessage() {
        return warningMessage;
    }
    public List<Long> getWarningTime() {
        return warningTime;
    }
    public String getWarningTitle() {
        return warningTitle;
    }
    public String getWarningSubtitle() {
        return warningSubtitle;
    }
    public List<Integer> getWarningTitleFade() {
        return warningTitleFade;
    }
    public boolean isLastSaved() {
        return lastSaved;
    }
    public boolean isResetting() {
        return resetting;
    }
    public HashMap<String, TimedReset> getTimedResets() {
        return timedResets;
    }
    public int getChunkCounter() {
        return chunkCounter;
    }
    public boolean isStartingReset() {
        return startingReset;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }
    public void setWorld(World world) {
        this.world = world;
    }
    public void setTime(List<String> time) {
        this.time = time;
    }
    public void setMessage(List<String> message) {
        this.message = message;
    }
    public void setSeed(String seed) {
        if (seed.equalsIgnoreCase("default")) this.seed = getWorld().getSeed();
        if (seed.equalsIgnoreCase("random")) randomSeed = true;
        try {
            this.seed = Long.parseLong(seed);
        } catch (Exception e) {
            this.seed = Long.parseLong(seed.hashCode() + "");
        }

    }

    public void setEnvironment(String environment) {
        if (environment == null) return;
        if (environment.matches("(?i)end|the_end|ender")) {
            this.environment = World.Environment.THE_END;
        } else if (environment.matches("(?i)nether|the_nether|hell")) {
            this.environment = World.Environment.NETHER;
        } else if (environment.matches("(?i)overworld|world|normal|earth")) {
            this.environment = World.Environment.NORMAL;
        } else {
            this.environment = getWorld().getEnvironment();
        }
    }
    public void setGenerator(String generator) {
        if (generator == null) return;
        if (generator.equalsIgnoreCase("default")) {
            try {
                this.generator = getWorld().getGenerator().toString();
            } catch (Exception e) {
                this.generator = null;
            }
        } else {
            if (generator.equalsIgnoreCase("terra")) generator = "Terra:DEFAULT";
            this.generator = generator;
        }
    }

    public void setDefaultGamemode(String defaultGamemode) {
        this.defaultGamemode = defaultGamemode;
    }
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
    public void setInitialCommands(List<String> initialCommands) {
        this.initialCommands = initialCommands;
    }
    public void setSafeWorldEnabled(boolean safeWorldEnabled) {
        this.safeWorldEnabled = safeWorldEnabled;
    }
    public void setSafeWorld(String safeWorld) {
        this.safeWorld = safeWorld;
    }
    public void setSafeWorldDelay(long safeWorldDelay) {
        if (safeWorldDelay >= -1) {
            this.safeWorldDelay = safeWorldDelay;
        } else {
            this.safeWorldDelay = 0;
        }
    }
    public void setSafeWorldSpawn(String safeWorldSpawn) {
        this.safeWorldSpawn = safeWorldSpawn;
    }
    public void setWarningEnabled(boolean warningEnabled) {
        this.warningEnabled = warningEnabled;
    }
    public void setWarningMessage(List<String> warningMessage) {
        this.warningMessage = warningMessage;
    }
    public void setWarningTime(List<Long> warningTime) {
        this.warningTime = (ArrayList<Long>) warningTime;
    }
    public void setWarningTitle(String warningTitle) {
        this.warningTitle = warningTitle;
    }
    public void setWarningSubtitle(String warningSubtitle) {
        this.warningSubtitle = warningSubtitle;
    }
    public void setWarningTitleFade(List<Integer> warningTitleFade) {
        this.warningTitleFade = warningTitleFade;
    }
    public void setLastSaved(boolean lastSaved) {
        this.lastSaved = lastSaved;
    }

}
