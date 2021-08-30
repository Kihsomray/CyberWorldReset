package net.zerotoil.cyberworldreset.commands;

import net.zerotoil.cyberworldreset.CyberWorldReset;
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

                if (main.langUtils().CheckPermMSG(player, "player-about")) return true;

                sender.sendMessage(main.langUtils().getColor("&a&lCyber&f&lWorldRegen &fv" + main.getDescription().getVersion() + " &7(&7<SPIGOT LINK>&7).", false));
                sender.sendMessage(main.langUtils().getColor("&fDeveloped by &a" + main.getDescription().getAuthors().toString()
                        .replace("[", "").replace("]", "") + "&f.", false));
                sender.sendMessage(main.langUtils().getColor("&7MESSAGE LINE 1", false));
                sender.sendMessage(main.langUtils().getColor("&7MESSAGE LINE 2", false));
                return true;

            }

            if (args[0].matches("(?i)reload")) {

                if (main.langUtils().CheckPermMSG(player, "admin-reload")) return true;

                main.lang().getMsg("reloading").send(player, true, new String[]{}, new String[]{});
                main.worlds().cancelTimers();
                main.loadCache();
                main.lang().getMsg("reloaded").send(player, true, new String[]{}, new String[]{});
                return true;

            }

            if (args[0].matches("(?i)confirm")) {
                if (confirmation.containsKey(player)) {
                    main.worlds().getWorld(confirmation.get(player)).regenWorld(player);
                    confirmation.remove(player);
                    return true;
                } else {
                    main.lang().getMsg("confirmation-not-required").send(player, true, new String[]{}, new String[]{});
                }
            }

        }

        if (args.length == 2) {


            if (args[0].matches("(?i)regen")) {

                if (main.worlds().getWorlds().isEmpty()) {
                    main.lang().getMsg("no-setups-found").send(player, true, new String[]{}, new String[]{});
                    return true;
                }
                if (!main.worlds().getWorlds().containsKey(args[1])) {
                    main.lang().getMsg("setup-doesnt-exist").send(player, true, new String[]{"world"}, new String[]{args[1]});
                    return true;
                }

                if (main.config().isConfirmationEnabled()) {

                    if (confirmation.containsKey(player)) {
                        main.lang().getMsg("confirmation-needed").send(player, true, new String[]{"world"}, new String[]{args[1]});
                        return true;
                    }

                    confirmation.put(player, args[1]);
                    String time = main.langUtils().formatTime(main.config().getConfirmationSeconds());
                    main.lang().getMsg("confirm-regen").send(player, true, new String[]{"world", "time"},
                            new String[]{args[1], time});

                    (new BukkitRunnable() {

                        @Override
                        public void run() {
                            if (confirmation.containsKey(player)) {
                                confirmation.remove(player);
                                main.lang().getMsg("confirmation-expired").send(player, true, new String[]{"world"}, new String[]{args[1]});
                            }
                        }

                    }).runTaskLater(main, 20L * main.config().getConfirmationSeconds());
                } else {
                    main.worlds().getWorld(args[1]).regenWorld(player);
                }

                return true;

            }

            if (args[0].matches("(?i)save")) {
                if (main.worlds().getWorlds().isEmpty()) {
                    main.lang().getMsg("no-setups-found").send(player, true, new String[]{}, new String[]{});
                    return true;
                }
                if (!main.worlds().getWorlds().containsKey(args[1])) {
                    main.lang().getMsg("setup-doesnt-exist").send(player, true, new String[]{"world"}, new String[]{args[1]});
                    return true;
                }
                main.worlds().getWorld(args[1]).saveWorld(player, true);


            }

        }

        // TODO Finish commands

        return main.langUtils().sendHelpMSG(player);

    }

}
