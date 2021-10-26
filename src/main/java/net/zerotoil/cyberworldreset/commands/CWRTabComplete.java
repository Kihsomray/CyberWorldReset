package net.zerotoil.cyberworldreset.commands;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CWRTabComplete implements TabCompleter {

    final private CyberWorldReset main;

    public CWRTabComplete (CyberWorldReset main) {
        this.main = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();

        List<String> args0 = new ArrayList<>();
        List<String> args0Comp = new ArrayList<>();
        String pPrefix = "CyberWorldReset.player.";
        String aPrefix = "CyberWorldReset.admin.";

        if (player.hasPermission(pPrefix + "about")) {
            args0.add("about");
            args0.add("version");
        }
        if (player.hasPermission(aPrefix + "reload")) args0.add("reload");
        if (player.hasPermission(aPrefix + "create")) args0.add("create");
        if (player.hasPermission(aPrefix + "reset")) {
            args0.add("confirm");
            args0.add("reset");
            args0.add("regen");
        }
        if (player.hasPermission(aPrefix + "save")) args0.add("save");
        if (main.langUtils().hasParentPerm(player, aPrefix + "edit")) args0.add("edit");
        if (player.hasPermission(aPrefix + "info")) args0.add("info");
        if (player.hasPermission(aPrefix + "list")) args0.add("list");

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], args0, args0Comp);
            Collections.sort(args0Comp);
            return args0Comp;
        }

        if (args.length == 2) {

            List<String> args1 = new ArrayList<>();
            List<String> args1Comp = new ArrayList<>();

            if (cmdReq(args[0], args0, "create")) {
                for (World world : Bukkit.getWorlds()) {
                    String worldName = world.getName();
                    if (main.worlds().getWorlds().containsKey(worldName)) continue;
                    args1.add(worldName);
                }
            }


            if (cmdReq(args[0], args0, "reset") || cmdReq(args[0], args0, "save") || cmdReq(args[0], args0, "edit")
                    || cmdReq(args[0], args0, "info") || cmdReq(args[0], args0, "regen")) {
                args1.addAll(main.worlds().getWorlds().keySet());
            }

            StringUtil.copyPartialMatches(args[1], args1, args1Comp);
            Collections.sort(args1Comp);
            return args1Comp;

        }

        if (args[0].equalsIgnoreCase("edit")) {

            if (args.length == 3) {

                List<String> args2 = new ArrayList<>();
                List<String> args2Comp = new ArrayList<>();

                String editPrefix = aPrefix + "edit.";
                if (player.hasPermission(editPrefix + "enable")) args2.add("setEnabled");
                if (player.hasPermission(editPrefix + "lastsaved")) args2.add("enableLastSaved");
                if (player.hasPermission(editPrefix + "timer")) {
                    args2.add("addTimer");
                    args2.add("delTimer");
                }
                if (player.hasPermission(editPrefix + "seed")) args2.add("setSeed");
                if (player.hasPermission(editPrefix + "message")) {
                    args2.add("addMessage");
                    args2.add("delMessage");
                }
                if (player.hasPermission(editPrefix + "commands")){
                    args2.add("addCommand");
                    args2.add("delCommand");
                }
                if (player.hasPermission(editPrefix + "warnings")) {
                    args2.add("enableWarning");
                    args2.add("addWarningMSG");
                    args2.add("delWarningMSG");
                    args2.add("addWarningTime");
                    args2.add("delWarningTime");
                }
                if (player.hasPermission(editPrefix + "safeworld")) {
                    args2.add("enableSafeWorld");
                    args2.add("setSafeWorld");
                    args2.add("setSafeWorldDelay");
                }

                StringUtil.copyPartialMatches(args[2], args2, args2Comp);
                Collections.sort(args2Comp);
                return args2Comp;

            }

            if (args.length == 4) {

                List<String> args3 = new ArrayList<>();
                List<String> args3Comp = new ArrayList<>();

                if (args[2].matches("(?i)setEnabled|enableLastSaved|enableWarning|enableSafeWorld")) {
                    args3.add("true");
                    args3.add("false");
                    args3.add("<boolean>");
                }

                if (args[2].matches("(?i)addTimer")) {
                    String format = "yyyy-MM-dd HH:mm";
                    args3.add(args[3] + format.substring(args[3].length()));
                    args3.add("<time format>");
                }

                if (args[2].matches("(?i)setSeed")) {
                    args3.add("DEFAULT");
                    args3.add("RANDOM");
                    args3.add("<seed>");
                }

                if (args[2].matches("(?i)setSafeWorld")) {
                    for (World w : Bukkit.getWorlds()) {
                        args3.add(w.getName().toString());
                    }
                    args3.add("<world>");
                }

                if (args[2].matches("(?i)delTimer|delMessage|delCommand|delWarningMSG|delWarningTime")) {
                    args3.add("<index>");
                }

                if (args[2].matches("(?i)addMessage|addWarningMSG")) args3.add("<message>");
                if (args[2].matches("(?i)addCommand")) args3.add("<command>");
                if (args[2].matches("(?i)addWarningTime|setSafeWorldDelay")) args3.add("<time>");

                StringUtil.copyPartialMatches(args[3], args3, args3Comp);
                Collections.sort(args3Comp);
                return args3Comp;

            }

            if (args.length == 5) {

                List<String> args4 = new ArrayList<>();
                List<String> args4Comp = new ArrayList<>();

                if (args[2].matches("(?i)addTimer")) {
                    String format = "HH:mm";
                    args4.add(args[4] + format.substring(args[4].length()));
                }

                StringUtil.copyPartialMatches(args[4], args4, args4Comp);
                Collections.sort(args4Comp);
                return args4Comp;

            }

        }

        return null;

    }

    private boolean cmdReq(String arg0, List<String> args0, String command) {
        return (arg0.equalsIgnoreCase(command) && args0.contains(command));
    }

}
