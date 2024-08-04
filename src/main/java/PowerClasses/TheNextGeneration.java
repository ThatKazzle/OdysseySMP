package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TheNextGeneration extends ParentPowerClass implements Listener {
    public HashMap<UUID, Long> cooldowns = new HashMap<>();

    public List<Player> fallDamageIgnoreList = new ArrayList<>();

    public TheNextGeneration(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);

        groundPoundAction(player);
    }

    public void groundPoundAction(Player player) {
        if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "end/dragon_egg")) {
            if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
                setCooldown(player.getUniqueId(), cooldowns, 60 * 2);

                fallDamageIgnoreList.add(player);

                player.setVelocity(new Vector(0, 3, 0));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setVelocity(new Vector(0, -3, 0));

                        fallDamageIgnoreList.add(player);

                        new BukkitRunnable() {
                            @Override
                            public void run() {

                                //player.sendMessage("" + player.getVelocity().getY());
                                if (player.getVelocity().getY() >= -0.08) {
                                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND, 1.f, 1.f);
                                    player.getWorld().playEffect(player.getLocation(), Effect.SMASH_ATTACK, 100);

                                    for (Player checkPlayer : Bukkit.getOnlinePlayers()) {
                                        if (checkPlayer.getWorld() == player.getWorld()) {
                                            if (checkPlayer.getLocation().distance(player.getLocation()) < 7) {
                                                if (checkPlayer != player) {
                                                    Vector dir = checkPlayer.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();

                                                    dir.setY(0);
                                                    dir.multiply(0.5);
                                                    dir.add(new Vector(0, 1, 0));

                                                    checkPlayer.sendMessage(dir.toString());

                                                    checkPlayer.setVelocity(dir);

                                                    if (checkPlayer.getHealth() - 6 <= 0) {
                                                        checkPlayer.setHealth(0);
                                                    } else {
                                                        checkPlayer.setHealth(checkPlayer.getHealth() - 6);
                                                    }

                                                    checkPlayer.damage(0.0001);
                                                }
                                            }
                                        }
                                    }

                                    summonDragonBreathCloud(player.getLocation());

                                    player.setVelocity(new Vector(0, 0.5, 0));

                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 0, 0);
                    }
                }.runTaskLater(plugin, 20);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            Player damagedPlayer = ((Player) event.getEntity());

            if (fallDamageIgnoreList.contains(damagedPlayer) && plugin.getConfig().getBoolean("players." + damagedPlayer.getName() + ".powers." + "end/dragon_egg")) {
                event.setCancelled(true);

                fallDamageIgnoreList.remove(damagedPlayer);
            }
        }
    }

    public void summonDragonBreathCloud(Location location) {
        // Create an AreaEffectCloud entity at the specified location
        AreaEffectCloud cloud = (AreaEffectCloud) location.getWorld().spawnEntity(location, EntityType.AREA_EFFECT_CLOUD);

        // Configure the AreaEffectCloud
        cloud.setDuration(200); // Duration in ticks (10 seconds)
        cloud.setRadius(3.0F); // Radius of the cloud
        cloud.setParticle(Particle.DRAGON_BREATH); // Particle effect
        cloud.setBasePotionType(PotionType.HARMING);
        cloud.setWaitTime(0); // Time before the cloud starts applying effects
        cloud.setRadiusPerTick(0); // No radius change over time

        // Set the custom potion effect to apply damage similar to the dragon's breath
        // You can configure the effect further if needed
        cloud.setCustomName("Dragon's Breath");
        cloud.setCustomNameVisible(false);

//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                if (!cloud.isDead()) {
//                    cloud.getNearbyEntities(3, 3, 3).forEach(entity -> {
//                        if (entity instanceof Player) {
//                            Player player = (Player) entity;
//                            player.damage(6.0); // Damage similar to dragon's breath
//                        }
//                    });
//                } else {
//                    cancel();
//                }
//            }
//        }.runTaskTimer(this, 0L, 20L); // Run task every second (20 ticks)
    }
}
