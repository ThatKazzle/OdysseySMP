package PowerClasses;

import com.destroystokyo.paper.profile.PlayerProfile;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HiredHelp extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    /** Golems that are still alive, so they can be removed if the server stops before they expire. */
    private final Set<IronGolem> activeGolems = new HashSet<>();

    private Player lastDamagedPlayer;

    SimpleS5 plugin;

    public HiredHelp(SimpleS5 plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void action(Player player) {

        if (isOnCooldown(player.getUniqueId(), cooldowns)) {
            cantUsePowerMessage(player, cooldowns, "Hired Help");
        } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
            setCooldown(player.getUniqueId(), cooldowns, 120);

            IronGolem golem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);

            golem.setAggressive(true);

            if (lastDamagedPlayer != null) {
                golem.setTarget(lastDamagedPlayer);
            } else {
                golem.setTarget(findClosestPlayer(player));
            }


            golem.setCustomName(ChatColor.AQUA + plugin.provider.getInfo(player).getName() + ChatColor.LIGHT_PURPLE + "'s Iron Golem");
            golem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(12);
            //golem.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, true));
            golem.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, true));

            activeGolems.add(golem);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                activeGolems.remove(golem);
                golem.remove();
            }, 20 * 20);
        }
    }

    @Override
    public void onShutdown() {
        for (IronGolem golem : activeGolems) {
            golem.remove();
        }
        activeGolems.clear();
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) return;
        Player damager = (Player) event.getDamager();

        if (!(event.getEntity() instanceof Player)) return;
        Player defender = (Player) event.getEntity();

        if (hasPower(damager, "adventure/summon_iron_golem")) {
            lastDamagedPlayer = defender;
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
