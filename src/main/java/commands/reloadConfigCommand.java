package commands;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class reloadConfigCommand implements CommandExecutor {

    SimpleS5 plugin;

    public reloadConfigCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.getName().equals("ItsKazzle") || player.getName().equals("AttiaTheGreat")) {
                this.plugin.saveDefaultConfig();
            }
            return true;
        } else {
            return false;
        }

    }
}
