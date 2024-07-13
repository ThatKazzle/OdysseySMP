package commands;

import kazzleinc.simples5.SimpleS5;
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
            Player player = (Player) sender;

            if (player.isOp()) {
                resetAllCooldowns(player);
                plugin.updateCooldownDisplay();
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
                            break;
                        case "husbandry/complete_catalogue":
                            break;
                        case "adventure/kill_all_mobs":
                            plugin.monstersClass.sphereCooldowns.replace(player.getUniqueId(), 0L);
                            break;
                        case "adventure/sniper_duel":
                            plugin.sniperDuelClass.cooldowns.replace(player.getUniqueId(), 0L);
                        case "nether/uneasy_alliance":
                            plugin.uneasyAllianceClass.cooldowns.replace(player.getUniqueId(), 0L);
                            break;
                    }
                }
            }
        }
    }
}
