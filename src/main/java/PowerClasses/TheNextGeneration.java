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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TheNextGeneration extends ParentPowerClass implements Listener {
    public HashMap<UUID, Long> cooldowns = new HashMap<>();
    public HashMap<UUID, Long> elytraCooldowns = new HashMap<>();

    public List<UUID> glidingPlayers = new ArrayList<>();

    public List<Player> fallDamageIgnoreList = new ArrayList<>();

    public TheNextGeneration(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);

        if (!player.isSneaking()) {
            groundPoundAction(player);
        } else {
            elytraFlightAction(player);
        }
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Elytra Launch: " + getCooldownTimeLeft(player.getUniqueId(), elytraCooldowns);
    }

    public void elytraFlightAction(Player player) {
        if (hasPower(player, "end/dragon_egg")) {
            if (!isOnCooldown(player.getUniqueId(), elytraCooldowns)) {
                setCooldown(player.getUniqueId(), elytraCooldowns, (60 * 3) + 30);

                Vector finalDir = player.getVelocity().add(new Vector(0, 2, 0));

                player.setVelocity(finalDir);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        glidingPlayers.add(player.getUniqueId());
                        player.setGliding(true);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                glidingPlayers.remove(player.getUniqueId());
                                player.setGliding(false);
                            }
                        }.runTaskLater(plugin, 20 * 5);
                    }
                }.runTaskLater(plugin, 20);
            }
        }
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
                                                    dir.multiply(0.7);
                                                    dir.add(new Vector(0, 0.3, 0));

                                                    checkPlayer.sendMessage(dir.toString());

                                                    checkPlayer.setVelocity(dir);

                                                    if (checkPlayer.getHealth() - 9 <= 0) {
                                                        checkPlayer.setHealth(0);
                                                    } else {
                                                        checkPlayer.setHealth(checkPlayer.getHealth() - 9);
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
        if (event.getEntity() instanceof Player) {
            Player damagedPlayer = ((Player) event.getEntity());
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if (fallDamageIgnoreList.contains(damagedPlayer) && plugin.getConfig().getBoolean("players." + damagedPlayer.getName() + ".powers." + "end/dragon_egg")) {
                    event.setCancelled(true);

                    fallDamageIgnoreList.remove(damagedPlayer);
                }
            }
        }
    }

    @EventHandler
    public void onEntityToggleGlideEvent(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!event.isGliding() && glidingPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);

                new BukkitRunnable() {
                    Player playerCheck = player;
                    @Override
                    public void run() {
                        if (playerCheck.isOnGround()) {
                            glidingPlayers.remove(playerCheck.getUniqueId());
                            playerCheck.setGliding(false);

                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 2);
                //glidingPlayers.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof AreaEffectCloud) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "end/dragon_egg")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public void summonDragonBreathCloud(Location location) {
        // Create an AreaEffectCloud entity at the specified location
        AreaEffectCloud cloud = (AreaEffectCloud) location.getWorld().spawnEntity(location, EntityType.AREA_EFFECT_CLOUD);

        // Configure the AreaEffectCloud
        cloud.setDuration(200); // Duration in ticks (10 seconds)
        cloud.setRadius(16.0F); // Radius of the cloud
        cloud.setParticle(Particle.DRAGON_BREATH); // Particle effect
        cloud.setBasePotionType(PotionType.STRONG_HARMING);
        cloud.setWaitTime(0); // Time before the cloud starts applying effects
        cloud.setRadiusPerTick(0); // No radius change over time
        cloud.setReapplicationDelay(10);

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
