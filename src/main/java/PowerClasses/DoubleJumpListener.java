package PowerClasses;

import it.unimi.dsi.fastutil.Hash;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.w3c.dom.Attr;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static kazzleinc.simples5.SimpleS5.roundDecimalNumber;

public class DoubleJumpListener extends ParentPowerClass implements Listener {

    public final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> rightClickedCooldowns = new HashMap<>();

    private final int cooldownTime = 10;
    private HashSet<Player> dashed = new HashSet<>(); //for making sure the player can only dash once



    public DoubleJumpListener(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        veryVeryFrighteningAction(playerName);
    }

    public HashMap<UUID, Long> getCooldownList() {
        return cooldowns;
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Item Disable: " + getCooldownTimeLeft(player.getUniqueId(), rightClickedCooldowns);
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
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer();
            Player clickedPlayer = (Player) event.getRightClicked();
            if (this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "adventure/very_very_frightening") && event.getHand() == EquipmentSlot.HAND && clickedPlayer.getInventory().getItemInMainHand().getType() != Material.AIR) {
                if (isOnCooldown(player.getUniqueId(), rightClickedCooldowns)) {
                    cantUsePowerMessage(player, rightClickedCooldowns, "Item Disable");
                } else if (player.isSneaking() && !isOnCooldown(player.getUniqueId(), rightClickedCooldowns)) {
                    replaceHeldItem(clickedPlayer, clickedPlayer.getInventory().getItemInMainHand(), player);

                    player.sendActionBar(ChatColor.GREEN + "Disabled " + ChatColor.AQUA + clickedPlayer.getName() + ChatColor.GREEN + "!");
                    setCooldown(player.getUniqueId(), rightClickedCooldowns, 30);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (event.getItemDrop().getItemStack().getType() == Material.STRUCTURE_VOID) {
            event.setCancelled(true);
        }
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

    private void replaceHeldItem(Player player, ItemStack stackToReplace, Player otherPlayer) {
        // Get the player's held item
        int origSlot = player.getInventory().getHeldItemSlot();
        ItemStack originalItem = stackToReplace;

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);

        player.sendActionBar(ChatColor.GREEN + "Your item has been " + ChatColor.RED + "disabled" + ChatColor.GREEN + " by " + ChatColor.RED + otherPlayer.getName() + ChatColor.GREEN + "!");

        // Create the barrier item
        ItemStack barrierItem = new ItemStack(Material.STRUCTURE_VOID);

        // Replace the held item with the barrier
        player.getInventory().setItem(origSlot, barrierItem);

        // Schedule a task to restore the original item after 3 seconds (60 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                int i = -1;
                for (ItemStack item : player.getInventory().getContents()) {
                    i++;
                    if (item.getType() == Material.STRUCTURE_VOID) {
                        player.getInventory().setItem(i, stackToReplace);
                        break;
                    }
                }

                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
                player.sendActionBar(ChatColor.GREEN + "Your item has been " + ChatColor.AQUA + "ENABLED" + ChatColor.GREEN + "!");
            }
        }.runTaskLater(this.plugin, 60L);
    }
}
