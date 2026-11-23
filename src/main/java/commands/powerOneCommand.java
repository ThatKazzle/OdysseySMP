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
            if (plugin.powerStore.hasPowersSection(player)) {
                enabledKeys.clear();
                enabledKeys.addAll(plugin.powerStore.getEnabledPowers(player));

                int playerMode = plugin.powerStore.getMode(player);

                if (enabledKeys.size() == 2) {
                    if (playerMode == 0) {
                        plugin.powerStore.setMode(player, 1);
                    } else if (playerMode == 1) {
                        plugin.powerStore.setMode(player, 0);
                    }
                } else if (enabledKeys.size() < 2) {
                    plugin.powerStore.setMode(player, 0);
                }

                this.plugin.updateCooldownDisplay();
            }


            return true;
        } else {
            return false;
        }
    }
}
