package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class HiredHelp extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    SimpleS5 plugin;

    public HiredHelp(SimpleS5 plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);

        if (isOnCooldown(player.getUniqueId(), cooldowns)) {
            cantUsePowerMessage(player, cooldowns, "Hired Help");
        } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
            setCooldown(player.getUniqueId(), cooldowns, 120);

            IronGolem golem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);

            golem.setAggressive(true);
            golem.setTarget(findClosestPlayer(player));
            golem.setCustomName(ChatColor.AQUA + player.getName() + ChatColor.LIGHT_PURPLE + "'s Iron Golem");

            golem.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 5, false, true));
            golem.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, true));

            plugin.getServer().getScheduler().runTaskLater(plugin, golem::remove, 20 * 20);
        }
    }

    public Player findClosestPlayer(Player targetPlayer) {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        // Iterate through all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(targetPlayer)) continue; // Skip the target player themselves

            // Calculate squared distance to avoid unnecessary square root calculation
            double distanceSquared = player.getLocation().distanceSquared(targetPlayer.getLocation());

            // Check if this player is closer than the current closest player
            if (distanceSquared < closestDistance) {
                closestPlayer = player;
                closestDistance = distanceSquared;
            }
        }

        return closestPlayer;
    }
}
