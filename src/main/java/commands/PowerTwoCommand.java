package commands;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** /power2: use the selected power. The logic is shared with the client mod in SimpleS5.usePower. */
public class PowerTwoCommand implements CommandExecutor {
    SimpleS5 plugin;

    public PowerTwoCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;

        // No powers entry at all: returning false shows the plugin.yml usage text, as before.
        if (!plugin.powerStore.hasPowersSection(player)) return false;

        plugin.usePower(player);
        return true;
    }
}
