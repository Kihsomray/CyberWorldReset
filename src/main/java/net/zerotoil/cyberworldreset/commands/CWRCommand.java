package net.zerotoil.cyberworldreset.commands;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import net.zerotoil.cyberworldreset.objects.WorldObject;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CWRCommand implements CommandExecutor {

    private CyberWorldReset main;
    private List<String> consoleCmds;

    public HashMap<Player, String> confirmation = new HashMap<>();

    public CWRCommand(CyberWorldReset main) {

        this.main = main;
        main.getCommand("cwr").setExecutor(this);
        consoleCmds = Arrays.asList("about", "reload");

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player player;
        String uuid;

        // console check
        if (!(sender instanceof Player) && !consoleCmds.contains(args[0])) {
            Bukkit.getLogger().warning("Console cannot use this command!");
            return true;
        } else if (!(sender instanceof Player)) {
            player = null;
            uuid = null;
        } else {
            player = (Player) sender;
            uuid = player.getUniqueId().toString();
        }

        if (args.length == 1) {

            if (args[0].matches("(?i)about|version")) {

                if (noPlayerPerm(player, "player.about")) return true;

                sender.sendMessage(main.langUtils().getColor("&b&lCyber&f&lWorldRegen &fv" + main.getDescription().getVersion() + " &7(&7<SPIGOT LINK>&7).", false));
                sender.sendMessage(main.langUtils().getColor("&fDeveloped by &b" + main.getDescription().getAuthors().toString()
                        .replace("[", "").replace("]", "") + "&f.", false));
                sender.sendMessage(main.langUtils().getColor("&7Easily regenerate worlds with little to no TPS drop. Simply set up a recursive", false));
                sender.sendMessage(main.langUtils().getColor("&7timer or a specific time & date you want the world to reset, and you’re all set!", false));
                return true;

            }

            // reload the plugin
            if (args[0].matches("(?i)reload")) {

                if (noPlayerPerm(player, "admin.reload")) return true;

                main.lang().getMsg("reloading").send(player, true, new String[]{}, new String[]{});
                main.worlds().cancelTimers();
                main.loadCache();
                main.lang().getMsg("reloaded").send(player, true, new String[]{}, new String[]{});
                return true;

            }

            // confirm regeneration of world
            if (args[0].matches("(?i)confirm")) {
                if (confirmation.containsKey(player)) {
                    main.worlds().getWorld(confirmation.get(player)).regenWorld(player);
                    confirmation.remove(player);
                } else {
                    main.lang().getMsg("confirmation-not-required").send(player, true, new String[]{}, new String[]{});
                }
                return true;
            }

            // regenerate current world
            if (args[0].matches("(?i)regen|regenerate|reset")) return regenWorld(player, player.getWorld().getName());

            // save current world
            if (args[0].matches("(?i)save|backup")) return saveWorld(player, player.getWorld().getName());

            // create a new setup for current world
            if (args[0].matches("(?i)create|setup")) return createSetup(player, player.getWorld().getName());

            // send the info of the current world
            if (args[0].matches("(?i)info")) return sendInfo(player, player.getWorld().getName());

        }

        if (args.length == 2) {


            // regenerate specific world
            if (args[0].matches("(?i)regen|regenerate|reset")) return regenWorld(player, args[1]);

            // save specific world
            if (args[0].matches("(?i)save|backup")) return saveWorld(player, args[1]);

            // create a new setup for specific world
            if (args[0].matches("(?i)create|setup")) return createSetup(player, args[1]);

            // send the info of specific world
            if (args[0].matches("(?i)info")) return sendInfo(player, args[1]);

        }

        if (args.length == 4) {

            if (args[0].equalsIgnoreCase("edit")) {
                if (!main.langUtils().hasParentPerm(player, "CyberWorldReset.admin")) return true;
                if (noSetupsExist(player)) return true;
                if (setupDoesNotExist(player, args[1])) return true;

                if (args[2].equalsIgnoreCase("setEnabled")) return setEnabled(player, args[1], args[3]);

                if (args[2].equalsIgnoreCase("enableLastSaved")) return enabledLastSaved(player, args[1], args[3]);

                // if (args[2].equalsIgnoreCase("addTimer")) return addTimer(player, args[1], args[3]);
                // TODO - Message

                if (args[2].equalsIgnoreCase("setSeed")) return setSeed(player, args[1], args[3]);

                // TODO - Safe World

            }

        }

        if (args[0].equalsIgnoreCase("edit") && args[2].equalsIgnoreCase("addTimer")) {

            String time = args[3];
            if (args.length > 4) time = time + " " + args[4];
            if (args.length == 6) time = time + " " + args[5];
            if (args.length > 4 && args.length < 7) {
                return addTimer(player, args[1], time);
            }
        }

        // TODO Finish commands

        return main.langUtils().sendHelpMSG(player);

    }

    private boolean notBoolean(Player player, String arg) {
        try {
            Boolean.parseBoolean(arg.toLowerCase());
            return false;
        } catch (Exception e) {
            main.lang().getMsg("invalid-command").send(player, true, new String[]{}, new String[]{});
            return true;
        }
    }

    private boolean regenWorld(Player player, String worldName) {
        if (noPlayerPerm(player, "admin.reset")) return true;
        if (noSetupsExist(player)) return true;
        if (setupDoesNotExist(player, worldName)) return true;

        if (main.config().isConfirmationEnabled()) {

            if (confirmation.containsKey(player)) {
                main.lang().getMsg("confirmation-needed").send(player, true, new String[]{"world"}, new String[]{worldName});
                return true;
            }

            confirmation.put(player, worldName);
            String time = main.langUtils().formatTime(main.config().getConfirmationSeconds());
            main.lang().getMsg("confirm-regen").send(player, true, new String[]{"world", "time"}, new String[]{worldName, time});

            (new BukkitRunnable() {

                @Override
                public void run() {
                    if (confirmation.containsKey(player)) {
                        confirmation.remove(player);
                        main.lang().getMsg("confirmation-expired").send(player, true, new String[]{"world"}, new String[]{worldName});
                    }
                }

            }).runTaskLater(main, 20L * main.config().getConfirmationSeconds());
        } else {
            main.worlds().getWorld(worldName).regenWorld(player);
        }
        return true;
    }

    private boolean saveWorld(Player player, String worldName) {
        if (noPlayerPerm(player, "admin.save")) return true;
        if (noSetupsExist(player)) return true;
        if (setupDoesNotExist(player, worldName)) return true;

        main.worlds().getWorld(worldName).saveWorld(player, true);
        return true;
    }
    private boolean createSetup(Player player, String worldName) {

        if (noPlayerPerm(player, "admin.create")) return true;
        if (main.worlds().getWorlds().containsKey(worldName)) {
            main.lang().getMsg("setup-already-exists").send(player, true, new String[]{"world"}, new String[]{worldName});
            return true;
        }
        main.worlds().createWorld(worldName, player);
        return true;
    }

    private boolean sendInfo(Player player, String worldName) {
        if (noPlayerPerm(player, "admin.info")) return true;
        if (noSetupsExist(player)) return true;
        if (setupDoesNotExist(player, worldName)) return true;

        infoMsg(player, "header", new String[]{"world"}, new String[]{worldName});
        sendHeader(player, 0);
        infoMsg(player, "enabled", new String[]{"boolean"}, new String[]{main.worlds().getWorld(worldName).isEnabled() + ""});
        infoMsg(player, "last-saved", new String[]{"boolean"}, new String[]{main.worlds().getWorld(worldName).isLastSaved() + ""});
        infoMsg(player, "seed", new String[]{"seed"}, new String[]{main.worlds().getWorld(worldName).getSeed() + ""});
        infoTimes(player, worldName);
        infoMessages(player, worldName);
        infoSafeWorld(player, worldName);
        infoWarning(player, worldName);
        infoCommands(player, worldName);
        infoMsg(player, "footer", new String[]{}, new String[]{});
        return true;
    }


    private void infoTimes(Player player, String worldName) {
        List<String> time = main.worlds().getWorld(worldName).getTime();
        if (time.isEmpty()) return;
        sendHeader(player, 1);
        sendList(player, time);
    }
    private void infoMessages(Player player, String worldName) {
        List<String> messages = main.worlds().getWorld(worldName).getMessage();
        if (messages.isEmpty()) return;
        sendHeader(player, 2);
        sendList(player, main.worlds().getWorld(worldName).getMessage());
    }
    private void infoSafeWorld(Player player, String worldName) {

        String sw = "safe-world-";
        sendHeader(player, 3);
        boolean safeWorldEnabled = getSetup(worldName).isSafeWorldEnabled();
        infoMsg(player, sw + "enabled", new String[]{"enabled"}, new String[]{safeWorldEnabled + ""});
        if (safeWorldEnabled) {
            infoMsg(player, sw + "world", new String[]{"world"}, new String[]{getSetup(worldName).getSafeWorld()});
            infoMsg(player, sw + "delay", new String[]{"delay"}, new String[]{getSetup(worldName).getSafeWorldDelay() + ""});
            infoMsg(player, sw + "spawn", new String[]{"spawn"}, new String[]{getSetup(worldName).getSafeWorldSpawn()});
        }
    }
    private void infoWarning(Player player, String worldName) {

        sendHeader(player, 4);
        boolean warningEnabled = main.worlds().getWorld(worldName).isWarningEnabled();
        infoMsg(player, "warning", new String[]{"enabled"}, new String[]{warningEnabled + ""});
        if (warningEnabled) sendList(player, main.worlds().getWorld(worldName).getWarningMessage());

        List<Long> warningTime = main.worlds().getWorld(worldName).getWarningTime();
        if (warningTime.isEmpty()) return;
        sendHeader(player, 5);
        sendList(player, warningTime);
    }
    private void infoCommands(Player player, String worldName) {
        List<String> commands = main.worlds().getWorld(worldName).getCommands();
        if (commands.isEmpty()) return;
        sendHeader(player, 6);
        sendList(player, commands); // this should work
    }


    private void infoMsg(Player player, String path, String[] placeholder, String[] replace) {
        main.lang().getMsg("info-" + path).send(player, false, placeholder, replace);
    }

    private void sendList(Player player, List list) {
        for (int i = 0; i < list.size(); i++) infoMsg(player, "list-format", new String[]{"id", "value"}, new String[]{i + "", list.get(i) + ""});
    }

    private void sendHeader(Player player, int index) {
        infoMsg(player, "list-header", new String[]{"header"}, new String[]{main.lang().getInfoHeaders().get(index)});
    }

    private boolean worldSetting(Player player, String world, String subPath, Object value) {
        main.files().getConfig("worlds").set("worlds." + world + "." + subPath, value);
        try {
            main.files().get("worlds").saveConfig();
            return true;
        } catch (Exception e) {
            main.lang().getMsg("setting-set-failed").send(player, true, new String[]{}, new String[]{});
            return false;
        }
    }
    private boolean setEnabled(Player player, String worldName, String value) {
        if (notBoolean(player, value)) return true;
        if (worldSetting(player, worldName, "enabled", Boolean.parseBoolean(value)))
            main.worlds().getWorld(worldName).setEnabled(Boolean.parseBoolean(value));
        return true;
    }
    private boolean enabledLastSaved(Player player, String worldName, String value) {
        if (notBoolean(player, value)) return true;
        if (worldSetting(player, worldName, "last-saved", true))
            main.worlds().getWorld(worldName).setLastSaved(Boolean.parseBoolean(value));
        return true;
    }
    private boolean addTimer(Player player, String worldName, String value) {
        List<String> timers = main.worlds().getWorld(worldName).getTime();
        timers.add(value);
        if (worldSetting(player, worldName, "settings.time", timers)) {
            main.worlds().getWorld(worldName).setTime(timers);
            main.worlds().getWorld(worldName).cancelTimers();
            main.worlds().getWorld(worldName).loadTimedResets();
        }
        return true;
    }
    private boolean setSeed(Player player, String worldName, String value) {
        if (worldSetting(player, worldName, "settings.seed", value))
            main.worlds().getWorld(worldName).setSeed(value);
        return true;
    }

    private boolean noSetupsExist(Player player) {
        if (main.worlds().getWorlds().isEmpty()) {
            main.lang().getMsg("no-setups-found").send(player, true, new String[]{}, new String[]{});
            return true;
        }
        return false;
    }
    private boolean setupDoesNotExist(Player player, String worldName) {
        if (!main.worlds().getWorlds().containsKey(worldName)) {
            main.lang().getMsg("setup-doesnt-exist").send(player, true, new String[]{"world"}, new String[]{worldName});
            return true;
        }
        return false;
    }
    private boolean noPlayerPerm(Player player, String permissionKey) {
        if (player == null) return false;
        if (!player.hasPermission("CyberWorldReset." + permissionKey)) {
            main.lang().getMsg("no-permission").send(player, true, new String[]{"permission", "player"}, new String[]{"CyberWorldReset." + permissionKey, player.getDisplayName()});
            return true;
        }
        return false;
    }

    private WorldObject getSetup(String worldName) {
        return main.worlds().getWorld(worldName);
    }

}
