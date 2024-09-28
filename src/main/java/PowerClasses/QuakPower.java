package PowerClasses;

import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class QuakPower extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();
    public final HashMap<UUID, Long> gapCooldowns = new HashMap<>();

    private HashSet<Player> dashed = new HashSet<>(); //for making sure the player can only dash once
    public QuakPower(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Gapple+: " + getCooldownTimeLeft(player.getUniqueId(), gapCooldowns);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (!player.isSneaking()) {
            doubleJumpAction(player);
        } else {
            gapAction(player);
        }
    }

    public void doubleJumpAction(Player player) {
        if (hasPower(player, "events/quaks_odyssey")) {
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                //cantUsePowerMessage(player, cooldowns, "Dash");
            } else if (!player.isOnGround() && !player.isInWater() && !isOnCooldown(player.getUniqueId(), cooldowns)) {
                Vector direction = player.getLocation().getDirection().normalize();

                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.f, 1.f);
                player.setVelocity(player.getVelocity().setY(1));

                ParticleUtils.createParticleRing(player.getLocation(), 1, 200, Particle.DUST, Color.fromRGB(255, 255, 0), 1);

                //ParticleUtils.createParticleRing(player.getLocation(), 1, 200, Particle.DUST, Color.PURPLE);

                setCooldown(player.getUniqueId(), cooldowns, 20);
                dashed.add(player);

                //the particles, looks super sigga
                new BukkitRunnable() {
                    Player checkPlayer = player;
                    @Override
                    public void run() {
                        if (!checkPlayer.isOnGround()) {
                            checkPlayer.getWorld().spawnParticle(Particle.DUST, checkPlayer.getLocation().add(new Vector(0, 1, 0)), 3, new Particle.DustOptions(Color.fromRGB(255, 255, 0), 2));
                        } else {
                            dashed.remove(player);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 0);
            }
        }
    }

    PotionEffect strengthEffect = new PotionEffect(PotionEffectType.STRENGTH, 20 * 15, 2, false, false, true);
    PotionEffect resistanceEffect = new PotionEffect(PotionEffectType.RESISTANCE, 20 * 15, 1, false, false, true);
    PotionEffect absorbEffect = new PotionEffect(PotionEffectType.ABSORPTION, 20 * 15, 1, false, false, true);

    public void gapAction(Player player) {
        if (!isOnCooldown(player.getUniqueId(), gapCooldowns)) {
            setCooldown(player.getUniqueId(), gapCooldowns, 100);

            player.addPotionEffect(strengthEffect);
            player.addPotionEffect(resistanceEffect);
            player.addPotionEffect(absorbEffect);

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.f, 1.f);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (this.plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "events/quaks_odyssey") && dashed.contains(player)) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);

                    dashed.remove(player);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player hitPlayer = (Player) event.getEntity();

            if (dashed.contains(damager)) {
                hitPlayer.setNoDamageTicks(0);
                hitPlayer.damage(event.getFinalDamage());
            }
        }
    }
}
