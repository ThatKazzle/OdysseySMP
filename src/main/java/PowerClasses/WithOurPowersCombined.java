package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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

    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "husbandry/froglights")) {
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                cantUsePowerMessage(player, cooldowns, "Odyssey Stealer");
            } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
                Location startLocation = player.getEyeLocation();

                Vector dir = player.getEyeLocation().getDirection().normalize();

                RayTraceResult result = player.getWorld().rayTraceEntities(startLocation, dir, 4.5);

                player.sendMessage("result of ray trace: " + result.getHitEntity().getName());

                if (result.getHitEntity() != null && event.getRightClicked() instanceof Player) {

                    setCooldown(player.getUniqueId(), cooldowns, 60 * 10);

                    Player targetPlayer = (Player) event.getRightClicked();

                    String targetPlayerEnabledKey = plugin.getPlayerPowersList(targetPlayer).get(plugin.getConfig().getInt("players." + targetPlayer.getName() + ".mode"));

                    plugin.getConfig().set("players." + player.getName() + ".powers." + targetPlayerEnabledKey, true);
                    plugin.getConfig().set("players." + targetPlayer.getName() + ".powers." + targetPlayerEnabledKey, false);

                    player.sendMessage(ChatColor.RED + "You stole " + ChatColor.AQUA + targetPlayerEnabledKey + ChatColor.RED + " from " + ChatColor.AQUA + targetPlayer.getName() + "!");
                    targetPlayer.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.RED + " stole " + ChatColor.AQUA + targetPlayerEnabledKey + "!");

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        plugin.getConfig().set("players." + player.getName() + ".powers." + targetPlayerEnabledKey, false);
                        plugin.getConfig().set("players." + targetPlayer.getName() + ".powers." + targetPlayerEnabledKey, true);

                        player.sendMessage(ChatColor.GREEN + "you lost your stolen power, and it has been given back to them.");
                        targetPlayer.sendMessage(ChatColor.GREEN + "You have been given your power back.");
                    }, 30 * 20); //change this to 2 minutes
                } else {
                    player.sendMessage(ChatColor.RED + "You didn't hit a player!");
                }
            }
        }
    }
}
