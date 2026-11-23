package commands;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadConfigCommand implements CommandExecutor {

    SimpleS5 plugin;

    public ReloadConfigCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("simples5.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }

        // Re-reads config.yml from disk. Player data is in data.yml and is not touched.
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "SimpleS5 config.yml reloaded.");
        return true;
    }
}
