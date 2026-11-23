package commands;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** /power1: switch between your two powers. The logic is shared with the client mod in SimpleS5.cyclePowerSlot. */
public class powerOneCommand implements CommandExecutor {
    SimpleS5 plugin;

    public powerOneCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;

        plugin.cyclePowerSlot(player);
        return true;
    }
}
