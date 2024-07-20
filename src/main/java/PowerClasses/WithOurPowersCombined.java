package PowerClasses;

import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
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
        stealerAction(plugin.getServer().getPlayer(playerName));
    }

    public void stealerAction(Player player) {
        RayTraceResult result = plugin.getServer().getWorld("world").rayTraceEntities(player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(2)), player.getEyeLocation().getDirection().normalize(), 45);

        if (result.getHitEntity() == null) return;

        ParticleUtils.createParticleRing(result.getHitPosition().toLocation(player.getWorld()), 1, 20, Particle.DUST, Color.RED);

        //checks to make sure everything lines up right
        if (result.getHitEntity() == null) return;

        if (!(result.getHitEntity() instanceof Player)) return;

        Player targetPlayer = ((Player) result.getHitEntity());

        if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "husbandry/froglights")) {
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                cantUsePowerMessage(player, cooldowns, "Odyssey Stealer");
            } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
                if (result.getHitEntity() != null) {

                    setCooldown(player.getUniqueId(), cooldowns, 60 * 10);

                    String targetPlayerEnabledKey;

                    if (plugin.getPlayerPowersList(targetPlayer).get(plugin.getConfig().getInt("players." + targetPlayer.getName() + ".mode")) != null) {
                        targetPlayerEnabledKey = plugin.getPlayerPowersList(targetPlayer).get(plugin.getConfig().getInt("players." + targetPlayer.getName() + ".mode"));
                    } else {
                        player.sendMessage("That player doesn't have a power you can steal!");
                        return;
                    }



                    plugin.getConfig().set("players." + player.getName() + ".powers." + "husbandry/froglights", false);
                    plugin.getConfig().set("players." + targetPlayer.getName() + ".powers." + targetPlayerEnabledKey, false);

                    plugin.getConfig().set("players." + player.getName() + ".powers." + targetPlayerEnabledKey, true);

                    player.sendMessage(ChatColor.RED + "You stole " + ChatColor.AQUA + targetPlayerEnabledKey + ChatColor.RED + " from " + ChatColor.AQUA + targetPlayer.getName() + "!");
                    targetPlayer.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.RED + " stole " + ChatColor.AQUA + targetPlayerEnabledKey + "!");

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        plugin.getConfig().set("players." + player.getName() + ".powers." + targetPlayerEnabledKey, false);
                        plugin.getConfig().set("players." + player.getName() + ".powers." + "husbandry/froglights", true);

                        plugin.getConfig().set("players." + targetPlayer.getName() + ".powers." + targetPlayerEnabledKey, true);

                        player.sendMessage(ChatColor.GREEN + "you lost your stolen power, and it has been given back to them.");
                        targetPlayer.sendMessage(ChatColor.GREEN + "You have been given your power back.");
                    }, 20 * 120);
                } else {
                    player.sendMessage(ChatColor.RED + "You didn't hit a player!");
                }
            }
        }
    }
}
