package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CompleteCatalogue extends ParentPowerClass implements Listener {

    public HashMap<UUID, Long> cooldowns = new HashMap<>();

    SimpleS5 plugin;
    public CompleteCatalogue(SimpleS5 plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void action(String playerName) {

    }

    public void giveCataloguePower(Player player) {
//        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(24);
//        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0, true, true));
    }

    public void removeCataloguePower(Player player) {
//        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
//        player.removePotionEffect(PotionEffectType.LUCK);
    }

    @EventHandler
    public void onPlayerTakeDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "husbandry/complete_catalogue") && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }

//            if (player.getHealth() - event.getFinalDamage() <= 0 && plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "husbandry/complete_catalogue")) {
//                event.setCancelled(true);
//
//                applyTotemEffects(player);
//                playTotemAnimation(player);
//
//                player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
//                player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation(), 30);
//            }
        }
    }

//    @EventHandler
//    public void onPlayerDeath(PlayerDeathEvent event) {
//        // Restore the player's health and cancel the death
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                if (event.getEntity().isDead()) {
//                    // Prevent the player from dying
//                    event.getEntity().setHealth(event.getEntity().getMaxHealth());
//                    event.getEntity().spigot().respawn();
//                    event.getEntity().sendMessage("You were saved from death!");
//                }
//            }
//        }.runTaskLater(this.plugin, 1L); // Schedule to run 1 tick later to ensure the player is still alive
//    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "husbandry/complete_catalogue") && !isOnCooldown(player.getUniqueId(), cooldowns)) {
                if (player.getHealth() - event.getFinalDamage() <= 0 && player.getInventory().getItemInOffHand().getType() != Material.TOTEM_OF_UNDYING) {
                    setCooldown(player.getUniqueId(), cooldowns, 10 * 60);

                    event.setCancelled(true);

                    ItemStack offhandItem = player.getInventory().getItemInOffHand();

                    player.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.damage(20);
                            player.getInventory().setItemInOffHand(offhandItem);
                            player.setHealth(0.5f);
                            applyTotemEffects(player);
                        }
                    }.runTaskLater(plugin, 6L);

                }
            }
        }
    }

    private void playTotemAnimation(Player player) {
        try {
            // Get the Player Connection
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = nmsPlayer.getClass().getField("b").get(nmsPlayer);

            // Get the PacketPlayOutEntityStatus packet
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityStatus");
            Constructor<?> packetConstructor = packetClass.getConstructor(Class.forName("net.minecraft.world.entity.Entity"), byte.class);

            // Create the packet
            Object packet = packetConstructor.newInstance(nmsPlayer, (byte) 35);

            // Send the packet to the player
            Method sendPacketMethod = playerConnection.getClass().getMethod("a", Class.forName("net.minecraft.network.protocol.Packet"));
            sendPacketMethod.invoke(playerConnection, packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyTotemEffects(Player player) {
        // Apply Absorption effect (4 additional health points for 5 seconds)
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 5, 1));

        // Apply Regeneration effect (heals 1 health point every 0.5 seconds for 45 seconds)
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 45, 1));

        // Apply Fire Resistance effect (prevents fire damage for 40 seconds)
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40 * 20, 0));
    }
}
