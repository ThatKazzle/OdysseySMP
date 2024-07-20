package PowerClasses;

import it.unimi.dsi.fastutil.Hash;
import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.w3c.dom.Attr;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static kazzleinc.simples5.SimpleS5.roundDecimalNumber;

public class DoubleJumpListener extends ParentPowerClass implements Listener {

    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    private final int cooldownTime = 10;
    private HashSet<Player> dashed = new HashSet<>(); //for making sure the player can only dash once

    public HashMap<UUID, Long> zeusCooldowns = new HashMap<>();



    public DoubleJumpListener(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        if (plugin.getServer().getPlayer(playerName).isOnGround()) {
            zeusAction(Objects.requireNonNull(plugin.getServer().getPlayer(playerName)));
        } else {
            veryVeryFrighteningAction(playerName);
        }

    }

    public HashMap<UUID, Long> getCooldownList() {
        return cooldowns;
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Zeus Bolt: " + getCooldownTimeLeft(player.getUniqueId(), zeusCooldowns);
    }

    private void veryVeryFrighteningAction(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        //player.sendMessage("Recieved, method called!");
        if (!player.isOnGround() && this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "adventure/very_very_frightening")) { // there is a line like this in every event to make sure nothing gets called if they dont have the advancement
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                cantUsePowerMessage(player, cooldowns, "Dash");
            } else if (!player.isOnGround() && !player.isInWater() && !isOnCooldown(player.getUniqueId(), cooldowns)) {
                Vector direction = player.getLocation().getDirection().normalize();
                player.setVelocity(direction.multiply(1.5));

                //ParticleUtils.createParticleRing(player.getLocation(), 1, 200, Particle.DUST, Color.PURPLE);

                setCooldown(player.getUniqueId(), cooldowns, cooldownTime);
                dashed.add(player);
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "adventure/very_very_frightening") && dashed.contains(player)) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);

                    dashed.remove(player);
                }
            }
        }
    }

    public void zeusAction(Player player) {
        if (this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "adventure/very_very_frightening")) {
            if (isOnCooldown(player.getUniqueId(), zeusCooldowns)) {
                cantUsePowerMessage(player, zeusCooldowns, "Zeus Bolt");
            } else if (!isOnCooldown(player.getUniqueId(), zeusCooldowns)) {
                strikeLightningEffect(player, player.getLocation());

                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 10, 1, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 1, false, false, true));

                setCooldown(player.getUniqueId(), zeusCooldowns, 120);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // Check if the entity being damaged is a player
        Entity damager = event.getDamager();
        if (damager instanceof Player) {
            Player damagerPlayer = (Player) damager;
            if (damagerPlayer.getInventory().getItemInMainHand().getType() == Material.STRUCTURE_VOID) {
                event.setCancelled(true);
            }
        }
    }

//    @EventHandler
//    public void onPlayerMoveEvent(PlayerMoveEvent event) {
//        Player player = event.getPlayer();
//        if (this.plugin.getConfig().getBoolean("players." + player.getName() + "powers." + "adventure/very_very_frightening")) {
//            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
//                if (player.isOnGround() && dashed.contains(player)) {
//                    dashed.remove(player);
//                }
//            }, 5);
//        }
//    }



    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (event.getItemDrop().getItemStack().getType() == Material.STRUCTURE_VOID) {
            event.setCancelled(true);
        }
    }

    public void strikeLightningEffect(Player player, Location location) {
        World world = player.getWorld();
        world.strikeLightningEffect(location);
    }

    public static boolean rayTraceHitsPlayer(Player player, double maxDistance) {
        // Get player's eye location and direction
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();

        // Calculate end location of the ray trace
        Vector maxDistanceVec = direction.multiply(maxDistance);
        Location endLocation = eyeLocation.add(maxDistanceVec);

        // Perform ray trace
        Entity hitEntity = player.getWorld().rayTraceEntities(eyeLocation, direction, maxDistance, entity -> entity instanceof Player && !entity.equals(player)).getHitEntity();

        // Check if the ray trace hit another player
        return hitEntity instanceof Player;
    }
}
