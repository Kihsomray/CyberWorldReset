package net.zerotoil.cyberworldreset.objects;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;

public class WorldObject {

    final private CyberWorldReset main; // req
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
    private List<String> commands = new ArrayList<>(); // not req

    // safe world module - not req
    private boolean safeWorldEnabled; // boolean defaults to false
    private String safeWorld;
    private long safeWorldDelay;
    private String safeWorldSpawn; // check Location object to do so

    // warning module - not req
    private boolean warningEnabled; // boolean defaults to false
    private List<String> warningMessage = new ArrayList<>();
    private ArrayList<Long> warningTime = new ArrayList<>();

    // timed resets module
    private HashMap<String, TimedReset> timedResets= new HashMap<>();

    // teleport player module
    private List<Player> tpPlayers = new ArrayList<>();

    private int chunkRun;
    private int chunkRise;
    private long loadDelay;
    private ArrayList<Integer> chunkInfo;
    private int xChunk;
    private int zChunk;
    private int chunkNumber;
    private int chunkCounter;

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
        message = new ArrayList<>();
        message.add("");
        loadDelay = main.config().getLoadingDelay();

    }

    public boolean regenWorld(Player sender) {

        if (!enabled) return false;
        tpPlayersAway();

        // default world check
        if (Objects.equals(main.worldUtils().getLevelName(), worldName)) return regenFail("default-world-fail", sender);

        // can the world unload?
        if (!Bukkit.unloadWorld(worldName, false)) return regenFail("unload-failed", sender);

        // save before reset
        if (main.config().isSaveWorldBeforeReset())if (!saveWorld(null, false)) regenFail(null, null);

        // deletes old world files
        try {
            FileUtils.deleteDirectory(new File(main.getDataFolder().getParentFile().getParentFile(), worldName));
        } catch (Exception e) {
            return regenFail("file-delete-failed", sender);
        }

        // reset delay
        long resetDelay = main.config().getWorldResetDelay();
        if (main.config().getWorldResetDelay() <= 0) resetDelay = 1;

        (new BukkitRunnable() {

            @Override
            public void run() {

                // should spawn chunks load?

                // creates world
                WorldCreator finalWorld = new WorldCreator(worldName);
                if (!lastSaved) {
                    finalWorld.environment(environment);
                    if (randomSeed) seed = new Random().nextLong();
                    finalWorld.seed(seed);
                } else {
                    if (!rollbackWorld(sender)) {
                        regenFail(null, sender);
                        return;
                    }
                }
                finalWorld.createWorld();

                // ultra fast chunk loading
                if (main.config().getLoadingType().matches("(?i)ULTRA-FAST")) getWorld().loadChunk(getWorld().getSpawnLocation().getChunk());

                // fast, normal, safe, ultra-safe chunk loading
                if (main.config().getLoadingType().matches("(?i)FAST|NORMAL|SAFE|ULTRA-SAFE")) safeLoadChunks(main.config().getLoadRadius(), sender);
                else finishRegen(sender);

            }

        }).runTaskLater(main, resetDelay);

        return true;

    }

    private void tpPlayersAway() {
        // kicks or teleports player
        if (Objects.isNull(safeWorld) || safeWorld.equals(worldName) || (!safeWorldEnabled)) {

            System.out.println("regen is null");

            // TODO - When players rejoin, they may spawn in mid air or in a block. find a fix for this.

            main.onJoin().setServerOpen(false);
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().getName().equalsIgnoreCase(worldName)) continue;
                    player.kickPlayer(main.lang().getMsg("kick-reason").toString(false).replace("{world}", worldName));
                }
            }

        } else {

            System.out.println("regen is not null");

            if (getWorld().getEnvironment().toString().contains("THE_END")) {
                getWorld().getEnderDragonBattle().getBossBar().removeAll();
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().getName().equalsIgnoreCase(worldName)) continue;

                if (player.isDead()) player.kickPlayer(main.lang().getMsg("kick-reason").toString(false).replace("{world}", worldName));

                tpPlayers.add(player);

                main.lang().getMsg("teleporting-safe-world").send(player, true,
                        new String[]{"world", "safeWorld"}, new String[]{worldName, safeWorld});

                if (!safeWorldSpawn.equalsIgnoreCase("default")) {

                    player.teleport(main.worldUtils().getLocationFromString(safeWorld, safeWorldSpawn));

                } else {
                    player.teleport(Bukkit.getWorld(safeWorld).getSpawnLocation());
                    /*
                     * TODO - Add essentials as a soft depend & check for spawn
                     */
                }

                main.lang().getMsg("teleported-safe-world").send(player, true,
                        new String[]{"world", "safeWorld"}, new String[]{worldName, safeWorld});

            }
            main.onWorldChange().addClosedWorld(worldName);
        }
    }

    public boolean regenFail(String msgKey, Player player) {
        if (msgKey != null) main.lang().getMsg(msgKey).send(player, true, new String[]{"world"}, new String[]{worldName});
        main.onWorldChange().removeClosedWorld(worldName);
        main.onJoin().setServerOpen(true);
        return false;
    }

    private void safeLoadChunks(int radius, Player sender) {
        // Chunk spawnChunk = spawnPoint.getChunk();
        // int blockRadius = radius * 16;
        int width = radius * 2 + 1;
        int area = width * width;
        // int[] loadingTime = new int[width];
        Random random = new Random();
        chunkRun = 1;
        chunkRise = 1;
        chunkInfo = new ArrayList<>();
        // chunkInfo.add(random.nextInt(width + 1));

        int avgMsgInterval = (int) Math.round((random.nextInt(4) + 3) * (20.0 / loadDelay));
        System.out.println(avgMsgInterval);
        long numMsg = (area - (area % avgMsgInterval)) / avgMsgInterval;
        System.out.println(numMsg);
        for (int i = 0; i < numMsg; i++) {
            int test = (random.nextInt(avgMsgInterval) + avgMsgInterval * i);
            System.out.println(test);
            chunkInfo.add(test);
        }
        xChunk = 0;
        zChunk = 0;
        chunkNumber = 0;
        chunkNumber++;
        chunkCounter = 2;
        Bukkit.getLogger().info("The world is being created, please wait!");

        (new BukkitRunnable() {

            @Override
            public void run() {
                printChunkInfo(width);
                getWorld().loadChunk(getWorld().getSpawnLocation().getChunk());


                for (int i = 1; i <= radius; i++) {
                    xChunk++;
                    loadChunk(width, sender);
                    for (int a = 1; a <= ((i * 2) - 1); a++) {
                        zChunk--;
                        loadChunk(width, sender);
                    }
                    int w = i * 2;
                    for (int a = 1; a <= 3; a++) {
                        for (int b = w; b > 0; b--) {
                            if (a == 1) xChunk--;
                            else if (a == 2) zChunk++;
                            else xChunk++;
                            loadChunk(width, sender);
                        }
                    }
                }


            }


        }).runTaskLater(main, 240L * Math.round(20 / Lag.getTPS()));






        /* new BukkitRunnable() {
            public void run() {
                Bukkit.getScheduler().runTask(main, () -> {

                    int chunk = (chunkRise + width * (chunkRun - 1));
                    Bukkit.getLogger().info("Chunk: " + chunk);
                    if (chunkRise == chunkInfo.get(chunkRun - 1)) {
                        long percent = Math.round(((chunk + 0.0) / area) * 100 - 1);
                        String timeLeft = main.langUtils().formatTime(Math.round(((area * (loadDelay / 20.0)) - (chunk * (loadDelay / 20.0))) * 1));
                        printChunkInfo(percent, chunk, area, timeLeft);
                    }

                    getWorld().loadChunk(x + ((chunkRun - 1) * 16), z + ((chunkRise - 1) * 16));
                    if (chunkRun == width + 1) {
                        printChunkInfo(100, area, area, main.langUtils().formatTime(0));
                        finishRegen(sender);
                        this.cancel();
                        return;
                    }
                    if (chunkRise == width + 1) {
                        chunkInfo.add(random.nextInt(width + 1));
                        chunkRise = 1;
                        chunkRun++;
                    } else {
                        chunkRise++;
                    }
                });
            }
        }.runTaskTimer(main, 100L, loadDelay); */

        /*for (int i = 0; i < width; i++) {
            loadingTime[i] = random.nextInt(width);
            for (int a = 0; a < width; a++) {
                final int aF = a, iF = i;
                (new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (aF == loadingTime[iF]) {
                            long percent = Math.round((((aF + 1.0) + width * iF) / (width * width)) * 100 - 1);
                            int chunk = ((aF + 1) + width * iF);
                            String timeLeft = main.langUtils().formatTime(Math.round(((area * (loadDelay / 20.0)) - (chunk * (loadDelay / 20.0))) * (20 / Lag.getTPS())));
                            printChunkInfo(percent, chunk, area, timeLeft);
                        }
                        getWorld().loadChunk(x + (iF * 16), z + (aF * 16));
                        if ((iF == width - 1) && (aF == width - 1)) {
                            printChunkInfo(100, area, area, main.langUtils().formatTime(0));
                            finishRegen(sender);
                        }
                    }


                }).runTaskLater(main, 15L + ((a + 1) + i * width) * loadDelay);
            }
        }*/
    }

    private void loadChunk(int width, Player sender) {
        Location location = getWorld().getSpawnLocation();
        // System.out.println("Chunk init: " + chunkCounter + ", X: " + xChunk + ", Z: " + zChunk);
        final int xChunk = this.xChunk;
        final int zChunk = this.zChunk;

        (new BukkitRunnable() {

            @Override
            public void run() {
                chunkNumber++;
                // System.out.println("Chunk: " + chunkNumber + ", X: " + xChunk + ", Z: " + zChunk);
                if (chunkInfo.contains(chunkNumber)) printChunkInfo(width);
                getWorld().loadChunk(location.getBlockX() + xChunk * 16, location.getBlockZ() + zChunk * 16);
                if (chunkNumber == width * width) {
                    printChunkInfo(width);
                    finishRegen(sender);
                }
            }


        }).runTaskLater(main, 15L + Math.round(chunkCounter * loadDelay));
        chunkCounter++;

    }

    private void printChunkInfo(int width) {
        double tps = Lag.getTPS();
        System.out.printf("Loading [%s]: %3d%% | chunk: %5d/%d | ETA: %-10s | TPS %.2f%n", worldName, Math.round((chunkNumber + 0.0) / (width * width) * 100), chunkNumber,
                width * width, ChatColor.stripColor(main.langUtils().formatTime(Math.round(((width * width) - chunkNumber) * (loadDelay / 20.0) * (20.0 / tps)))), tps);
    }

    private void tpPlayersBack() {
        if (!safeWorldEnabled) return;

        Location spawnPoint = getWorld().getSpawnLocation();
        main.onDamage().setEnabled(true);
        main.onWorldChange().removeClosedWorld(worldName);
        List<Player> tempTpPlayers = tpPlayers;
        for (int i = 0; i < tempTpPlayers.size(); i++) {

            Player player = tempTpPlayers.get(i);
            if (!player.isOnline()) {
                tpPlayers.remove(player);
                continue;
            }
            main.lang().getMsg("teleporting-back").send(player, true, new String[]{"world", "safeWorld"}, new String[]{worldName, safeWorld});

            player.teleport(new Location(getWorld(), spawnPoint.getX(), getWorld().getHighestBlockYAt(spawnPoint.getBlockX(), spawnPoint.getBlockZ()), spawnPoint.getZ()));
            tpPlayers.remove(player);

            main.lang().getMsg("teleported-back").send(player, true, new String[]{"world", "safeWorld"}, new String[]{worldName, safeWorld});

        }

        // disables onDamage 1.8 fixer
        (new BukkitRunnable() {

            @Override
            public void run() {
                main.onDamage().setEnabled(false);
            }

        }).runTaskLater(main, 200L);

    }

    private void finishRegen(Player sender) {

        // sends successful regen to all players
        if ((message.size() != 1) || !message.get(0).equalsIgnoreCase(""))
            for (Player player : Bukkit.getOnlinePlayers())
                for (String i : message) player.sendMessage(main.langUtils().getColor(i.replace("{world}", worldName), true));

        System.out.println("regen 7");

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

        System.out.println("regen 8 - done");
        main.lang().getMsg("regen-success").send(sender, true, new String[]{"world"}, new String[]{worldName});
    }

    public boolean saveWorld(Player player, boolean saveWorld) {

        main.lang().getMsg("saving-world").send(player, true, new String[]{"world"}, new String[]{getWorldName()});
        File savedWorlds = new File(main.getDataFolder(),"saved_worlds");
        if (!savedWorlds.exists()) savedWorlds.mkdirs();
        if (saveWorld) {
            main.onWorldSave().addWorldToBackup(worldName);
            getWorld().save();
        } else {
            try {
                main.zipUtils().zip(worldName);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;

    }

    public boolean rollbackWorld(Player player){

        main.lang().getMsg("rolling-back-world").send(player, true, new String[]{"world"}, new String[]{worldName});

        File worldSave = main.zipUtils().getLastModified(worldName);
        if (worldSave == null) {
            main.lang().getMsg("rollback-failed").send(player, true, new String[]{"world"}, new String[]{worldName});
            return false;
        }
        try {
            main.zipUtils().unZip(worldSave.getName());
            main.lang().getMsg("rollback-success").send(player, true, new String[]{"world"}, new String[]{worldName});
            return true;
        } catch (IOException e) {
            main.lang().getMsg("rollback-failed").send(player, true, new String[]{"world"}, new String[]{worldName});
            e.printStackTrace();
            return false;
        }
    }

    public void loadTimedResets() {
        if (time.isEmpty()) return;
        for (String i : time) timedResets.put(i, new TimedReset(main, worldName, i, warningTime));
    }

    public void sendWarning(String unformatted) {

        String time = main.langUtils().formatTime(timedResets.get(unformatted).timeToReset());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != getWorld()) continue;
            for (String i : warningMessage) player.sendMessage(main.langUtils().getColor(i.replace("{world}", worldName).replace("{time}", time), true));
        }

    }

    public void cancelTimers() {
        if (timedResets.isEmpty()) return;
        for (TimedReset timedReset : timedResets.values()) timedReset.cancelAllTimers();
    }


    private World.Environment getEnvironment() {
        return Bukkit.getWorld(worldName).getEnvironment();
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

    public void setEnabled(boolean enabled) { // TODO - Add enabled checker
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
    public void setDefaultGamemode(String defaultGamemode) { // TODO - Add default gamemode
        this.defaultGamemode = defaultGamemode;
    }
    public void setCommands(List<String> commands) {
        this.commands = commands;
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
    public void setLastSaved(boolean lastSaved) {
        this.lastSaved = lastSaved;
    }
    public boolean isLastSaved() {
        return lastSaved;
    }

}