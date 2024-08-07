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
            String playerName = plugin.provider.getInfo(player).getName();

            if (this.plugin.getConfig().getConfigurationSection("players." + plugin.provider.getInfo(player).getName() + ".powers") != null) {
                if (!enabledKeys.isEmpty()) {
                    enabledKeys.clear();
                }

                for (String keys : this.plugin.getConfig().getConfigurationSection("players." + plugin.provider.getInfo(player).getName() + ".powers").getKeys(false)) {
                    String key = keys;
                    Boolean value = this.plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + key);

                    if (value) {
                        enabledKeys.add(key);
                    }
                }

                int playerMode = this.plugin.getConfig().getInt("players." + playerName + ".mode");

                if (enabledKeys.size() == 2) {
                    if (playerMode == 0) {

                        this.plugin.getConfig().set("players." + playerName + ".mode", 1);

                    } else if (playerMode == 1) {

                        this.plugin.getConfig().set("players." + playerName + ".mode", 0);

                    }

                } else if (enabledKeys.size() == 1) {
                    this.plugin.getConfig().set("players." + playerName + ".mode", 0);
                } else if (enabledKeys.isEmpty()) {
                    this.plugin.getConfig().set("players." + playerName + ".mode", 0);
                }

                this.plugin.updateCooldownDisplay();

                this.plugin.saveConfig();
            }


            return true;
        } else {
            return false;
        }
    }
}
