package commands;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class powerOneCommand implements CommandExecutor {
    SimpleS5 plugin;

    public List<String> enabledKeys = new ArrayList<>();

    public powerOneCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            String playerName = player.getName();

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

            switch (enabledKeys.get(this.plugin.getConfig().getInt("players." + playerName + ".mode") - 1)) {
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
            }

            return true;
        } else {
            return false;
        }
    }
}
