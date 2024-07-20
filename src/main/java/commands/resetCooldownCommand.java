package commands;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class resetCooldownCommand implements CommandExecutor {

    SimpleS5 plugin;

    public resetCooldownCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player cmdSender = (Player) sender;
            Player player2;
            if (args.length > 0 && plugin.getServer().getPlayer(args[0]) != null) {
                player2 = plugin.getServer().getPlayer(args[0]);
            } else {
                player2 = null;
            }

            if (cmdSender.isOp()) {
                resetAllCooldowns(cmdSender);
                plugin.updateCooldownDisplay();
            }

            if (player2 == null) {

            } else {
                if (args.length == 1) {
                    if (cmdSender.isOp()) {
                        resetAllCooldowns(player2);
                        plugin.updateCooldownDisplay();
                    } else {
                        cmdSender.sendMessage(ChatColor.RED + "You can't run that command!");
                    }
                } else {
                    usageMessage(cmdSender);
                }

            }

            return true;
        } else {
            return false;
        }
    }

    public void resetAllCooldowns(Player player) {
        List<String> enabledKeys = new ArrayList<>();

        if (this.plugin.getConfig().getConfigurationSection("players." + player.getName() + ".powers") != null) {
            for (String keys : this.plugin.getConfig().getConfigurationSection("players." + player.getName() + ".powers").getKeys(false)) {
                String key = keys;
                Boolean value = this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + key);

                if (value) {
                    enabledKeys.add(key);

                    switch (key) {
                        case "adventure/very_very_frightening":
                            plugin.vvfClass.cooldowns.replace(player.getUniqueId(), 0L);
                            break;
                        case "nether/all_effects":
                            plugin.hdwghClass.rightClickedCooldowns.replace(player.getUniqueId(), 0L);
                            break;
                        case "husbandry/complete_catalogue":
                            break;
                        case "adventure/kill_all_mobs":
                            plugin.monstersClass.sphereCooldowns.replace(player.getUniqueId(), 0L);
                            break;
                        case "adventure/sniper_duel":
                            plugin.sniperDuelClass.cooldowns.replace(player.getUniqueId(), 0L);
                            break;
                        case "nether/uneasy_alliance":
                            plugin.uneasyAllianceClass.cooldowns.replace(player.getUniqueId(), 0L);
                            break;
                        case "husbandry/froglights":
                            plugin.wopcClass.cooldowns.replace(player.getUniqueId(), 0L);
                            break;
                        case "nether/create_beacon":
                            plugin.beaconatorClass.cooldowns.replace(player.getUniqueId(), 0L);
                    }
                }
            }
        }
    }

    private void usageMessage(Player player) {
        player.sendMessage(ChatColor.RED + "Usage: /rc [player]");
    }

}
