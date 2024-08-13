package commands;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class PvpToggleCommand implements CommandExecutor {
    private SimpleS5 plugin;

    public PvpToggleCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player sender = (Player) commandSender;
            if (sender.isOp()) {
                plugin.pvpEnabled = !plugin.pvpEnabled;

                sender.sendMessage(ChatColor.GREEN + "PvP has been " + (plugin.pvpEnabled ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));

                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You ain't opped idiot");

                return false;
            }
        } else {

            return false;
        }
    }
}
