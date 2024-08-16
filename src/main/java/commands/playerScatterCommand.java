package commands;

import kazzleinc.simples5.RandomUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class playerScatterCommand implements CommandExecutor {

    SimpleS5 plugin;

    public playerScatterCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (player.isOp()) {
                if (args.length == 1) {
                    scatterPlayers(Integer.parseInt(args[0]), player.getLocation());
                } else {
                    player.sendMessage("Not right args dumbass");
                }

                return true;
            } else {
                player.sendMessage("You are not opped idiot");
                return false;
            }
        } else {
            return false;
        }
    }

    public void scatterPlayers(int radius, Location playerLocation) {
        for (Player checkPlayer : Bukkit.getOnlinePlayers()) {
            if (!checkPlayer.isOp()) {

                Location tpLocation = new Location(checkPlayer.getWorld(),
                        RandomUtils.getRandomIntInRange((int) (radius + playerLocation.getX()), (int) (radius + playerLocation.getX())),
                        255,
                        RandomUtils.getRandomIntInRange((int) (radius + playerLocation.getZ()), (int) (radius + playerLocation.getZ()))
                );

                RayTraceResult result = checkPlayer.getWorld().rayTraceBlocks(tpLocation, new Vector(0, -1, 0), 400, FluidCollisionMode.ALWAYS);

                checkPlayer.teleport(result.getHitPosition().toLocation(checkPlayer.getWorld()), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }
    }
}
