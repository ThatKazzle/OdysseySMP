package commands;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PowerTwoCommand implements CommandExecutor {
    SimpleS5 plugin;

    public List<String> enabledKeys = new ArrayList<>();

    public PowerTwoCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            String playerName = player.getName();
            if (this.plugin.getConfig().getConfigurationSection("players." + player.getName() + ".powers") != null) {
                if (!enabledKeys.isEmpty()) {
                    enabledKeys.clear();
                }

                for (String keys : this.plugin.getConfig().getConfigurationSection("players." + player.getName() + ".powers").getKeys(false)) {
                    String key = keys;
                    Boolean value = this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + key);

                    if (value) {
                        enabledKeys.add(key);
                    }
                }

                player.sendMessage(String.valueOf(this.plugin.getConfig().getInt("players." + playerName + ".mode")));

                if (!enabledKeys.isEmpty()) {
                    switch (enabledKeys.get(this.plugin.getConfig().getInt("players." + playerName + ".mode"))) {
                        case "adventure/very_very_frightening":
                            plugin.vvfClass.action(playerName);
                            break;
                        case "nether/all_effects":
                            plugin.hdwghClass.action(playerName);
                            break;
                        case "husbandry/complete_catalogue":
                            plugin.catalogueClass.action(playerName);
                            break;
                        case "adventure/kill_all_mobs":
                            plugin.monstersClass.action(playerName);
                            break;
                        case "adventure/sniper_duel":
                            plugin.sniperDuelClass.action(playerName);
                        case "nether/uneasy_alliance":
                            plugin.uneasyAllianceClass.action(playerName);
                            break;
                        case "husbandry/froglights":
                            plugin.wopcClass.action(playerName);
                            break;
                        case "adventure/summon_iron_golem":
                            plugin.hiredHelpClass.action(playerName);
                        case "nether/ride_strider_in_overworld_lava":
                            plugin.feelsLikeHomeClass.action(playerName);
                            break;
                    }

                    this.plugin.updateCooldownDisplay();
                } else {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.1f);
                    player.sendActionBar(ChatColor.RED + "You don't have any powers.");
                }

                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }

    }
}
