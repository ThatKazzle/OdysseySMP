package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class WithOurPowersCombined extends ParentPowerClass implements Listener {
    public HashMap<UUID, Long> cooldowns = new HashMap<>();

    public WithOurPowersCombined(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);

        if (plugin.getConfig().getBoolean("players." + playerName + ".powers." + "froglights")) {
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                cantUsePowerMessage(player, cooldowns, "Odyssey Stealer");
            } else if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                Location startLocation = player.getLocation();

                Vector dir = player.getEyeLocation().getDirection().normalize();

                RayTraceResult result = player.getWorld().rayTraceEntities(startLocation, dir, 4.5);

                if (result.getHitEntity() != null && result.getHitEntity() instanceof Player) {
                    setCooldown(player.getUniqueId(), cooldowns, 60 * 10);

                    Player targetPlayer = (Player) result.getHitEntity();

                    String targetPlayerEnabledKey = plugin.getPlayerPowersList(targetPlayer).get(plugin.getConfig().getInt("players." + targetPlayer.getName()));

                    plugin.getConfig().set("players." + player.getName() + ".powers." + targetPlayerEnabledKey, true);
                    plugin.getConfig().set("players." + targetPlayer.getName() + ".powers." + targetPlayerEnabledKey, false);

                    player.sendMessage(ChatColor.RED + "You stole " + ChatColor.AQUA + targetPlayer.getName() + ChatColor.RED + "'s power!");
                    targetPlayer.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.RED + " stole your current power!");

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        plugin.getConfig().set("players." + player.getName() + ".powers." + targetPlayerEnabledKey, false);
                        plugin.getConfig().set("players." + targetPlayer.getName() + ".powers." + targetPlayerEnabledKey, true);

                        player.sendMessage(ChatColor.GREEN + "you lost your stolen power, and it has been given back to them.");
                        targetPlayer.sendMessage(ChatColor.GREEN + "You have been given your power back.");
                    }, 30 * 20); //change this to 2 minutes
                }
            }
        }
    }
}
