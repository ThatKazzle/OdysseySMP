package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class CharlisPower extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();
    public final HashMap<UUID, Long> cobCooldowns = new HashMap<>();

    private HashSet<Player> dashed = new HashSet<>(); //for making sure the player can only dash once
    public CharlisPower(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Webbed: " + getCooldownTimeLeft(player.getUniqueId(), cobCooldowns);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (!player.isSneaking()) {
            dashAction(player);
        } else {
            cobAction(player);
        }
    }

    public void dashAction(Player player) {
        if (hasPower(player, "events/charli's_power")) {
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                //cantUsePowerMessage(player, cooldowns, "Dash");
            } else if (!player.isOnGround() && !player.isInWater() && !isOnCooldown(player.getUniqueId(), cooldowns)) {
                Vector direction = player.getLocation().getDirection().normalize();

                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.f, 1.f);
                player.setVelocity(direction.multiply(1.5));

                //ParticleUtils.createParticleRing(player.getLocation(), 1, 200, Particle.DUST, Color.PURPLE);

                setCooldown(player.getUniqueId(), cooldowns, 10);
                dashed.add(player);

                //the particles, looks super sigga
                new BukkitRunnable() {
                    Player checkPlayer = player;
                    @Override
                    public void run() {
                        if (!checkPlayer.isOnGround()) {
                            checkPlayer.getWorld().spawnParticle(Particle.DUST, checkPlayer.getLocation().add(new Vector(0, 1, 0)), 3, new Particle.DustOptions(Color.fromRGB(62, 14, 98), 3));
                        } else {
                            dashed.remove(player);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 0);
            }
        }
    }

    public void cobAction(Player player) {
        if (hasPower(player, "events/charli's_power")) {
            if (isOnCooldown(player.getUniqueId(), cobCooldowns)) {
                //cantUsePowerMessage(player, cooldowns, "Dash");
            } else if (!isOnCooldown(player.getUniqueId(), cobCooldowns)) {
                setCooldown(player.getUniqueId(), cobCooldowns, 150);

                for (int x = -2; x < 2; x++) {
                    player.getWorld().getBlockAt(player.getLocation().add(x, 0.5, 0)).setType(Material.COBWEB);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (this.plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "events/charli's_power") && dashed.contains(player)) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);

                    dashed.remove(player);
                }
            }
        }
    }
}
