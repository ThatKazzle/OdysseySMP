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

import java.util.Random;

public class playerScatterCommand implements CommandExecutor {

    SimpleS5 plugin;

    public playerScatterCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("scatter")) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;

                if (!player.isOp()) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }

                if (args.length != 1) {
                    player.sendMessage("Usage: /scatter <radius>");
                    return true;
                }

                int radius;
                try {
                    radius = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid radius. Please enter a valid number.");
                    return true;
                }

                Location center = player.getLocation();
                Random random = new Random();

                // Scatter all players within the radius
                for (Player target : Bukkit.getOnlinePlayers()) {
                    double angle = random.nextDouble() * 2 * Math.PI; // Random angle
                    double distance = random.nextDouble() * radius; // Random distance within the radius
                    double xOffset = distance * Math.cos(angle);
                    double zOffset = distance * Math.sin(angle);

                    Location targetLocation = center.clone().add(xOffset, 0, zOffset);
                    // Ensure the player is teleported to a safe location (above ground)
                    targetLocation.setY(center.getWorld().getHighestBlockYAt(targetLocation));

                    target.teleport(targetLocation);
                    target.sendMessage("You have been scattered to a random location!");
                }

                player.sendMessage("Players have been scattered within a radius of " + radius + " blocks.");
                return true;
            } else {
                commandSender.sendMessage("This command can only be run by a player.");
            }
        }
        return false;
    }
}
