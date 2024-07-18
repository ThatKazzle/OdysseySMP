package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static kazzleinc.simples5.SimpleS5.roundDecimalNumber;

public class SniperDuel extends ParentPowerClass implements Listener {
    SimpleS5 plugin;

    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    private final List<Player> affectedPlayers = new ArrayList<>();

    public SniperDuel(SimpleS5 plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public void action(String playerName) {
        Player player = this.plugin.getServer().getPlayer(playerName);

        if (this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "adventure/sniper_duel")) {
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.f, 1.f);

                double timeLeft = roundDecimalNumber((cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()), 1) / 1000;
            } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
                //setCooldown(player.getUniqueId(), cooldowns, 60);

                StringBuilder displayableList = new StringBuilder();
                for (Player playerCheck : this.plugin.getServer().getOnlinePlayers()) {
                    if (playerCheck != player && player.getLocation().distance(playerCheck.getLocation()) <= 50) {
                        affectedPlayers.add(playerCheck);
                        applyEffects(playerCheck);
                        PotionEffect strength = new PotionEffect(PotionEffectType.STRENGTH, 200, 1, false, false, true);
                        player.addPotionEffect(strength);

                        displayableList.append(ChatColor.AQUA + playerCheck.getName()).append(ChatColor.GREEN + ", ");
                    }
                }

                if (!affectedPlayers.isEmpty()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.f, 1.f);

                    displayableList.setLength(displayableList.length() - 2);
                    //   ItsKazzle, jkitsaustin, myutsu
                    if (displayableList.toString().split(", ").length > 1) {
                        displayableList.insert(displayableList.length() - affectedPlayers.getLast().getName().length(), ChatColor.GREEN + " and " + ChatColor.AQUA);
                    }

                    player.sendActionBar(ChatColor.GREEN + "affected " + displayableList + "!");
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.5f);

                    player.sendActionBar(ChatColor.RED + "No players were affected.");
                }

                setCooldown(player.getUniqueId(), cooldowns, 120);
                affectedPlayers.clear();
                displayableList.setLength(0);
            }
        }
    }

    @EventHandler
    public void onShootBowEvent(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player shooter = (Player) event.getEntity();
        if (!(event.getProjectile() instanceof Arrow)) {
            return;
        }

        if (!isOnCooldown(shooter.getUniqueId(), cooldowns)) {
            return;
        }

        Arrow arrow = (Arrow) event.getProjectile();
        Player target = getTargetedPlayer((Player) event.getEntity(), 1000);

        if (target != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (arrow.isDead() || target.isDead() || !arrow.isValid() || !target.isValid()) {
                        this.cancel();
                        return;
                    }

                    Vector toTarget = target.getEyeLocation().toVector().subtract(arrow.getLocation().toVector());
                    Vector direction = arrow.getVelocity().clone().normalize();

                    Vector newDirection = direction.multiply(0.6).add(toTarget.normalize().multiply(0.4)).normalize();

                    arrow.setVelocity(newDirection.multiply(arrow.getVelocity().length()));
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    private Player getNearestPlayer(Player shooter) {
        Player nearestPlayer = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(shooter) || !player.getWorld().equals(shooter.getWorld())) {
                continue;
            }

            double distanceSquared = player.getLocation().distanceSquared(shooter.getLocation());
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestPlayer = player;
            }
        }

        return nearestPlayer;
    }

    private Player getTargetedPlayer(Player shooter, double maxDistance) {
        Location eyeLocation = shooter.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        List<Entity> nearbyEntities = shooter.getNearbyEntities(maxDistance, maxDistance, maxDistance);

        Player closestPlayer = null;
        double closestDistanceSquared = Double.MAX_VALUE;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player && !entity.equals(shooter)) {
                Player player = (Player) entity;
                Location playerLocation = player.getLocation().add(0, 1, 0); // Target the head

                Vector toPlayer = playerLocation.toVector().subtract(eyeLocation.toVector());
                double distanceSquared = eyeLocation.distanceSquared(playerLocation);

                if (distanceSquared <= maxDistance * maxDistance && toPlayer.normalize().dot(direction) > 0.85) {
                    // Check if the player is in the shooter's line of sight
                    if (closestPlayer == null || distanceSquared < closestDistanceSquared) {
                        closestDistanceSquared = distanceSquared;
                        closestPlayer = player;
                    }
                }
            }
        }

        return closestPlayer;
    }

    private void applyEffects(Player player) {
        // Apply the glowing effect for 10 seconds (200 ticks)
        PotionEffect glowing = new PotionEffect(PotionEffectType.GLOWING, 200, 1, false, false, true);
        PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 200, 3, false, false, true);
        player.addPotionEffect(glowing);
        player.addPotionEffect(weakness);
    }
}
