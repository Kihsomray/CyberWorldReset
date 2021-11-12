package net.zerotoil.cyberworldreset.commands;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import net.zerotoil.cyberworldreset.objects.WorldObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

        if (args.length == 1) return argsLen1(sender, player, args);

        if (args.length == 2) return argsLen2(sender, player, args);

        if (args.length > 3 && args[0].matches("(?i)edit")) {
            if (args[2].matches("(?i)addTimer")) {

                String time = args[3];
                if (args.length > 4) time = time + " " + args[4];
                if (args.length == 6) time = time + " " + args[5];
                if (args.length > 4 && args.length < 7) {
                    return addTimer(player, args[1], time);
                }
            }

            if (args[2].matches("(?i)addMessage")) {
                return addMessage(player, args[1], convertToString(args));
            }

            if (args[2].matches("(?i)addCommand")) {
                return addCommand(player, args[1], convertToString(args));
            }

            if (args[2].matches("(?i)addWarningMSG")) {
                return addWarningMSG(player, args[1], convertToString(args));
            }

            if (args[2].matches("(?i)setSafeWorldSpawn")) {
                String message = args[3];
                if (!message.contains(",")) message += ",";
                if (args.length >= 5) for (int i = 4; i < args.length; i++){
                    message += " " + args[i];
                    if (!args[i].contains(",")) message += ",";
                }
                return setSafeWorldSpawn(player, args[1], message.substring(0, message.length() - 1));
            }

            if (args[2].matches("(?i)setWarningTitle")) {
                return setWarningTitle(player, args[1], convertToString(args));
            }

            if (args[2].matches("(?i)setWarningSubtitle")) {
                return setWarningSubtitle(player, args[1], convertToString(args));
            }

            if (args[2].matches("(?i)setGenerator")) {
                return setGenerator(player, args[1], convertToString(args));
            }

        }

        if (args.length == 4) return argsLen4(sender, player, args);

        return main.langUtils().sendHelpMSG(player);

    }

    private String convertToString(String[] args) {
        String message = args[3];
        if (args.length >= 5) for (int i = 4; i < args.length; i++) message += " " + args[i];
        return message;
    }

    private boolean argsLen1(CommandSender sender, Player player, String[] args) {
        if (args[0].matches("(?i)about|version")) {

            if (noPlayerPerm(player, "player.about")) return true;

            sender.sendMessage(main.langUtils().getColor("&b&lCyber&f&lWorldReset &fv" + main.getDescription().getVersion() + " &7(&7&nhttps://bit.ly/2YSlqYq&7).", false));
            sender.sendMessage(main.langUtils().getColor("&fDeveloped by &b" + main.getAuthors() + "&f.", false));
            sender.sendMessage(main.langUtils().getColor("&7Easily regenerate worlds with little to no TPS drop. Simply set up a recursive", false));
            sender.sendMessage(main.langUtils().getColor("&7timer or a specific time & date you want the world to reset, and you’re all set!", false));
            return true;

        }

        // reload the plugin
        if (args[0].matches("(?i)reload")) {

            if (noPlayerPerm(player, "admin.reload")) return true;

            main.lang().getMsg("reloading").send(player, true, new String[]{}, new String[]{});
            if (main.worlds().isWorldResetting()) {
                main.lang().getMsg("resetting-error").send(player, true, new String[]{}, new String[]{});
                return true;
            }
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

        String playerWorld = player.getWorld().getName();

        // regenerate current world
        if (args[0].matches("(?i)regen|regenerate|reset")) return regenWorld(player, playerWorld);

        // save current world
        if (args[0].matches("(?i)save|backup")) return saveWorld(player, playerWorld);

        // create a new setup for current world
        if (args[0].matches("(?i)create|setup")) return createSetup(player, playerWorld);

        // send the info of the current world
        if (args[0].matches("(?i)info")) return sendInfo(player, playerWorld);

        // send the info of the current world
        if (args[0].matches("(?i)list")) return sendWorldList(player);

        return main.langUtils().sendHelpMSG(player);

    }

    private boolean argsLen2(CommandSender sender, Player player, String[] args) {
        // regenerate specific world
        if (args[0].matches("(?i)regen|regenerate|reset")) return regenWorld(player, args[1]);

        // save specific world
        if (args[0].matches("(?i)save|backup")) return saveWorld(player, args[1]);

        // create a new setup for specific world
        if (args[0].matches("(?i)create|setup")) return createSetup(player, args[1]);

        // send the info of specific world
        if (args[0].matches("(?i)info")) return sendInfo(player, args[1]);

        return main.langUtils().sendHelpMSG(player);

    }

    private boolean argsLen4(CommandSender sender, Player player, String[] args) {

        if (args[0].matches("(?i)edit")) {
            // if (!main.langUtils().hasParentPerm(player, "CyberWorldReset.admin")) return true;
            if (noSetupsExist(player)) return true;
            if (setupDoesNotExist(player, args[1])) return true;

            if (main.worlds().getWorld(args[1]).isResetting()) {
                main.lang().getMsg("resetting-error").send(player, true, new String[]{}, new String[]{});
                return true;
            }

            switch (args[2].toLowerCase()){

                case "setenabled": return setEnabled(player, args[1], args[3]);
                case "enablelastsaved": return enabledLastSaved(player, args[1], args[3]);
                case "enablesafeworld": return enabledSafeWorld(player, args[1], args[3]);
                case "setseed": return setSeed(player, args[1], args[3]);
                case "setsafeworld": return setSafeWorld(player, args[1], args[3]);
                case "setsafeworlddelay": return setSafeWorldDelay(player, args[1], args[3]);
                case "addwarningtime": return addWarningTime(player, args[1], args[3]);
                case "enablewarning": return enableWarning(player, args[1], args[3]);
                case "delcommand": return delCommand(player, args[1], args[3]);
                case "delmessage": return delMessage(player, args[1], args[3]);
                case "deltimer": return delTimer(player, args[1], args[3]);
                case "delwarningmsg": return delWarningMessage(player, args[1], args[3]);
                case "delwarningtime": return delWarningTime(player, args[1], args[3]);
                case "setenvironment": return setEnvironment(player, args[1], args[3]);
                //case "setsafeworldspawn": return setSafeWorldSpawn(player, args[1], args[3]);



                default: return main.langUtils().sendHelpMSG(player);

            }

        }

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
    private boolean notLong(Player player, String arg) {
        try {
            Long.parseLong(arg);
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
        if (main.worlds().getWorld(worldName).isResetting()) {
            main.lang().getMsg("resetting-error").send(player, true, new String[]{}, new String[]{});
            return true;
        }
        main.worlds().getWorld(worldName).saveWorld(player, true);
        return true;
    }
    private boolean createSetup(Player player, String worldName) {

        if (noPlayerPerm(player, "admin.create")) return true;
        if (main.worlds().getWorlds().containsKey(worldName)) {
            main.lang().getMsg("setup-already-exists").send(player, true, new String[]{"world"}, new String[]{worldName});
            return true;
        }
        if (worldName.equalsIgnoreCase(main.worldUtils().getLevelName())) {
            main.lang().getMsg("default-world-fail").send(player, true, new String[]{"world"}, new String[]{worldName});
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
        infoMsg(player, "generator", new String[]{"generator"}, new String[]{main.worlds().getWorld(worldName).getGenerator()});
        infoMsg(player, "environment", new String[]{"environment"}, new String[]{main.worlds().getWorld(worldName).getEnvironment()});

        infoTimes(player, worldName);
        infoMessages(player, worldName);
        infoSafeWorld(player, worldName);
        infoWarning(player, worldName);
        infoCommands(player, worldName);
        infoMsg(player, "footer", new String[]{}, new String[]{});
        return true;
    }

    private boolean sendWorldList(Player player) {
        if (noPlayerPerm(player, "admin.list")) return true;
        if (noSetupsExist(player)) return true;

        main.lang().getMsg("list-header").send(player, false, new String[]{}, new String[]{});
        for (String world : main.worlds().getWorlds().keySet()) {
            main.lang().getMsg("list-info").send(player, false, new String[]{"world", "enabled"}, new String[]{world, main.worlds().getWorld(world).isEnabled() + ""});
        }
        main.lang().getMsg("list-footer").send(player, false, new String[]{}, new String[]{});
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
            infoMsg(player, sw + "world", new String[]{"world"}, new String[]{getSetup(worldName).getSafeWorld() + ""});
            infoMsg(player, sw + "delay", new String[]{"delay"}, new String[]{getSetup(worldName).getSafeWorldDelay() + ""});
            infoMsg(player, sw + "spawn", new String[]{"spawn"}, new String[]{getSetup(worldName).getSafeWorldSpawn() + ""});
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

        if (main.worlds().getWorld(worldName).getWarningTitle() != null)
            infoMsg(player, "title", new String[]{"title"}, new String[]{main.worlds().getWorld(worldName).getWarningTitle()});
        if (main.worlds().getWorld(worldName).getWarningSubtitle() != null)
            infoMsg(player, "subtitle", new String[]{"subtitle"}, new String[]{main.worlds().getWorld(worldName).getWarningSubtitle()});
    }
    private void infoCommands(Player player, String worldName) {
        List<String> commands = main.worlds().getWorld(worldName).getCommands();
        if (commands.isEmpty()) return;
        sendHeader(player, 6);
        sendList(player, commands);
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
            main.lang().getMsg("setting-set-success").send(player, true, new String[]{}, new String[]{});
            return true;
        } catch (Exception e) {
            main.lang().getMsg("setting-set-failed").send(player, true, new String[]{}, new String[]{});
            return false;
        }
    }
    private boolean setEnabled(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.enable")) return true;

        if (notBoolean(player, value)) return true;
        if (worldSetting(player, worldName, "enabled", Boolean.parseBoolean(value))) {
            main.worlds().getWorld(worldName).setEnabled(Boolean.parseBoolean(value));
            main.worlds().getWorld(worldName).cancelTimers();
            main.worlds().getWorld(worldName).loadTimedResets();
        }
        return true;
    }
    private boolean enabledLastSaved(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.lastsaved")) return true;

        if (notBoolean(player, value)) return true;
        if (worldSetting(player, worldName, "last-saved", Boolean.parseBoolean(value)))
            main.worlds().getWorld(worldName).setLastSaved(Boolean.parseBoolean(value));
        return true;
    }
    private boolean enabledSafeWorld(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.safeworld")) return true;

        if (notBoolean(player, value)) return true;
        boolean enable = Boolean.parseBoolean(value);
        if (enable) {
            if (main.worlds().getWorld(worldName).getSafeWorld() == null){
                main.lang().getMsg("safeworld-not-set").send(player, true, new String[]{"world"}, new String[]{worldName});
                return true;
            }

            if (Bukkit.getWorld(main.worlds().getWorld(worldName).getSafeWorld()) == null) {
                main.lang().getMsg("world-not-exist").send(player, true, new String[]{"world"}, new String[]{worldName});
                return true;
            }
            if (Bukkit.getWorld(main.worlds().getWorld(worldName).getSafeWorld()) == main.worlds().getWorld(worldName).getWorld()) {
                main.lang().getMsg("same-world-fail").send(player, true, new String[]{"world"}, new String[]{worldName});
                return true;
            }
        }
        if (worldSetting(player, worldName, "settings.safe-world.enabled", enable)) {
            main.worlds().getWorld(worldName).setSafeWorldEnabled(enable);
            infoSafeWorld(player, worldName);
        }
        return true;
    }
    private boolean addTimer(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.timer")) return true;
        if (noSetupsExist(player)) return true;
        if (setupDoesNotExist(player, worldName)) return true;

        List<String> timers = main.worlds().getWorld(worldName).getTime();
        timers.add(value);
        if (worldSetting(player, worldName, "settings.time", timers)) {
            main.worlds().getWorld(worldName).setTime(timers);
            main.worlds().getWorld(worldName).cancelTimers();
            main.worlds().getWorld(worldName).loadTimedResets();
            infoTimes(player, worldName);
        }
        return true;
    }

    private boolean addMessage(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.message")) return true;
        if (noSetupsExist(player)) return true;
        if (setupDoesNotExist(player, worldName)) return true;

        List<String> messages = main.worlds().getWorld(worldName).getMessage();
        messages.add(value);
        if (worldSetting(player, worldName, "settings.messages", messages) || worldSetting(player, worldName, "settings.message", null)) {
            main.worlds().getWorld(worldName).setMessage(messages);
            infoMessages(player, worldName);
        }
        return true;
    }

    private boolean addWarningMSG(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.warnings")) return true;
        if (noSetupsExist(player)) return true;
        if (setupDoesNotExist(player, worldName)) return true;

        List<String> messages = main.worlds().getWorld(worldName).getWarningMessage();
        messages.add(value);
        if (worldSetting(player, worldName, "settings.warning.message", messages)) {
            main.worlds().getWorld(worldName).setWarningMessage(messages);
            infoWarning(player, worldName);
        }
        return true;
    }

    private boolean setWarningTitle(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.warnings")) return true;
        if (noSetupsExist(player)) return true;
        if (setupDoesNotExist(player, worldName)) return true;

        if (worldSetting(player, worldName, "settings.warning.title.title", value)) {
            main.worlds().getWorld(worldName).setWarningTitle(value);
            infoWarning(player, worldName);
        }
        return true;
    }

    private boolean setWarningSubtitle(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.warnings")) return true;
        if (noSetupsExist(player)) return true;
        if (setupDoesNotExist(player, worldName)) return true;

        if (worldSetting(player, worldName, "settings.warning.title.sub-title", value)) {
            main.worlds().getWorld(worldName).setWarningSubtitle(value);
            infoWarning(player, worldName);
        }
        return true;
    }

    private boolean addCommand(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.commands")) return true;
        if (noSetupsExist(player)) return true;
        if (setupDoesNotExist(player, worldName)) return true;

        List<String> commands = main.worlds().getWorld(worldName).getCommands();
        commands.add(value);
        if (worldSetting(player, worldName, "settings.commands", commands)) {
            main.worlds().getWorld(worldName).setCommands(commands);
            infoCommands(player, worldName);
        }
        return true;
    }

    private boolean addWarningTime(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.warnings")) return true;

        if (notLong(player, value)) return true;
        long number = Math.max(Long.parseLong(value), 0);
        List<Long> times = main.worlds().getWorld(worldName).getWarningTime();
        times.add(number);
        if (worldSetting(player, worldName, "settings.warning.time", times)) {
            main.worlds().getWorld(worldName).setWarningTime(times);
            main.worlds().getWorld(worldName).cancelTimers();
            main.worlds().getWorld(worldName).loadTimedResets();
            infoWarning(player, worldName);
        }
        return true;
    }

    private boolean enableWarning(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.warnings")) return true;

        if (notBoolean(player, value)) return true;
        boolean bool = Boolean.parseBoolean(value);
        if (bool && main.worlds().getWorld(worldName).getWarningMessage().size() == 0) {
            main.lang().getMsg("no-messages-found").send(player, true, new String[]{}, new String[]{});
            return true;
        }
        if (worldSetting(player, worldName, "settings.warning.enabled", bool)) {
            main.worlds().getWorld(worldName).setWarningEnabled(bool);
            infoWarning(player, worldName);
        }
        return true;
    }

    private boolean delCommand(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.commands")) return true;

        if (notLong(player, value)) return true;
        int number = (int) Math.max(Long.parseLong(value), 0);
        List<String> commands = main.worlds().getWorld(worldName).getCommands();
        int size = commands.size();
        if (size <= number) {
            main.lang().getMsg("invalid-index").send(player, true, new String[]{"min", "max"}, new String[]{0 + "", (size - 1) + ""});
            return true;
        }
        commands.remove(number);
        if (worldSetting(player, worldName, "settings.commands", commands)) {
            main.worlds().getWorld(worldName).setCommands(commands);
            infoCommands(player, worldName);
        }
        return true;
    }

    private boolean delMessage(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.message")) return true;

        if (notLong(player, value)) return true;
        int number = (int) Math.max(Long.parseLong(value), 0);
        List<String> messages = main.worlds().getWorld(worldName).getMessage();
        int size = messages.size();
        if (size <= number) {
            main.lang().getMsg("invalid-index").send(player, true, new String[]{"min", "max"}, new String[]{0 + "", Math.min(size - 1, 0) + ""});
            return true;
        }
        messages.remove(number);
        if (worldSetting(player, worldName, "settings.messages", messages) || worldSetting(player, worldName, "settings.message", null)) {
            main.worlds().getWorld(worldName).setMessage(messages);
            infoMessages(player, worldName);
        }
        return true;
    }

    private boolean delTimer(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.timer")) return true;
        if (main.worlds().isWorldResetting()) {
            main.lang().getMsg("resetting-error").send(player, true, new String[]{}, new String[]{});
            return true;
        }
        if (notLong(player, value)) return true;
        int number = (int) Math.max(Long.parseLong(value), 0);
        List<String> timers = main.worlds().getWorld(worldName).getTime();
        int size = timers.size();
        if (size <= number) {
            main.lang().getMsg("invalid-index").send(player, true, new String[]{"min", "max"}, new String[]{0 + "", (size - 1) + ""});
            return true;
        }
        timers.remove(number);
        if (worldSetting(player, worldName, "settings.time", timers)) {
            main.worlds().cancelTimers();
            main.loadCache();
            infoTimes(player, worldName);
        }
        return true;
    }

    private boolean delWarningMessage(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.warnings")) return true;

        if (notLong(player, value)) return true;
        int number = (int) Math.max(Long.parseLong(value), 0);
        List<String> messages = main.worlds().getWorld(worldName).getWarningMessage();
        int size = messages.size();
        if (size <= number) {
            main.lang().getMsg("invalid-index").send(player, true, new String[]{"min", "max"}, new String[]{0 + "", (size - 1) + ""});
            return true;
        }
        messages.remove(number);
        if (worldSetting(player, worldName, "settings.warning.message", messages)) {
            main.worlds().getWorld(worldName).setWarningMessage(messages);
            infoWarning(player, worldName);
        }
        return true;
    }

    private boolean delWarningTime(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.warnings")) return true;

        if (notLong(player, value)) return true;
        int number = (int) Math.max(Long.parseLong(value), 0);
        List<Long> times = main.worlds().getWorld(worldName).getWarningTime();
        int size = times.size();
        if (size <= number) {
            main.lang().getMsg("invalid-index").send(player, true, new String[]{"min", "max"}, new String[]{0 + "", (size - 1) + ""});
            return true;
        }
        times.remove(number);
        if (worldSetting(player, worldName, "settings.warning.time", times)) {
            main.worlds().getWorld(worldName).setWarningTime(times);
            infoWarning(player, worldName);
        }
        return true;
    }

    private boolean setSafeWorldDelay(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.safeworld")) return true;

        long delay;
        try {
            delay = Long.parseLong(value);
        } catch (Exception e) {
            delay = 10;
        }
        if (worldSetting(player, worldName, "settings.safe-world.delay", delay)) {
            main.worlds().getWorld(worldName).setSafeWorldDelay(delay);
            infoSafeWorld(player, worldName);
        }
        return true;
    }

    private boolean setSeed(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.seed")) return true;

        if (worldSetting(player, worldName, "settings.seed", value))
            main.worlds().getWorld(worldName).setSeed(value);
        return true;
    }

    private boolean setEnvironment(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.environment")) return true;
        if (!value.matches("(?i)end|the_end|ender|nether|the_nether|hell|overworld|world|normal|earth|default")) {
            main.lang().getMsg("invalid-command").send(player);
            return true;
        }

        if (worldSetting(player, worldName, "settings.environment", value))
            main.worlds().getWorld(worldName).setEnvironment(value);
        return true;
    }

    private boolean setGenerator(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.generator")) return true;
        if (noSetupsExist(player)) return true;
        if (setupDoesNotExist(player, worldName)) return true;

        if (!value.equalsIgnoreCase("default")) main.lang().getMsg("custom-generator-warning").send(player, true);

        if (worldSetting(player, worldName, "settings.generator", value))
            main.worlds().getWorld(worldName).setGenerator(value);
        return true;
    }

    private boolean setSafeWorld(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.safeworld")) return true;

        if (Bukkit.getWorld(value) == null) {
            main.lang().getMsg("world-not-exist").send(player, true, new String[]{"world"}, new String[]{value});
            return true;
        }
        if (Bukkit.getWorld(value) == main.worlds().getWorld(worldName).getWorld()) {
            main.lang().getMsg("same-world-fail").send(player, true, new String[]{"world"}, new String[]{value});
            return true;
        }
        if (worldSetting(player, worldName, "settings.safe-world.world", value)) {
            main.worlds().getWorld(worldName).setSafeWorld(value);
            infoSafeWorld(player, worldName);
        }
        return true;
    }

    private boolean setSafeWorldSpawn(Player player, String worldName, String value) {
        if (noPlayerPerm(player, "admin.edit.safeworld")) return true;
        String finalValue = "DEFAULT";
        if (main.worldUtils().areCoordinates(value)) {
            Location loc = main.worldUtils().getLocationFromString(main.worlds().getWorld(worldName).getSafeWorld(), value);
            finalValue = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
        } else if (!value.equalsIgnoreCase("default")) {
            main.lang().getMsg("invalid-command").send(player);
            return true;
        }
        if (worldSetting(player, worldName, "settings.safe-world.spawn", finalValue)) {
            main.worlds().getWorld(worldName).setSafeWorldSpawn(finalValue);
            infoSafeWorld(player, worldName);
        }

        return true;


    }

    private boolean noSetupsExist(Player player) {
        if (main.worlds().getWorlds().isEmpty()) {
            main.lang().getMsg("no-setups-found").send(player);
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
