package commands;

import kazzleinc.simples5.PowerRegistry;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** /rc [player]: ops reset all power cooldowns for themselves, or for the named player only. */
public class ResetCooldownCommand implements CommandExecutor {

    SimpleS5 plugin;

    public ResetCooldownCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player cmdSender)) return false;

        if (!cmdSender.isOp()) {
            cmdSender.sendMessage(ChatColor.RED + "You can't run that command!");
            return true;
        }

        if (args.length > 1) {
            usageMessage(cmdSender);
            return true;
        }

        Player target = cmdSender;
        if (args.length == 1) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                cmdSender.sendMessage(ChatColor.RED + "No online player named " + args[0] + ".");
                return true;
            }
        }

        resetAllCooldowns(target);
        plugin.updateCooldownDisplay();

        if (target != cmdSender) {
            cmdSender.sendMessage(ChatColor.GREEN + "Reset all cooldowns for " + target.getName() + ".");
        }
        return true;
    }

    /** Clears every cooldown of every power for the player, so no power can be missed. */
    public void resetAllCooldowns(Player player) {
        for (PowerRegistry.PowerDefinition def : plugin.powers.all()) {
            def.power().resetCooldowns(player.getUniqueId());
        }
    }

    private void usageMessage(Player player) {
        player.sendMessage(ChatColor.RED + "Usage: /rc [player]");
    }

}
