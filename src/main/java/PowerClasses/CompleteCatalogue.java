package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class CompleteCatalogue extends ParentPowerClass implements Listener {

    SimpleS5 plugin;
    public CompleteCatalogue(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {

    }

    public void giveCataloguePower(Player player) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(24);
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0, true, true));
    }

    public void removeCataloguePower(Player player) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.removePotionEffect(PotionEffectType.LUCK);
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));

        // Apply Regeneration effect (heals 1 health point every 0.5 seconds for 45 seconds)
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));

        // Apply Fire Resistance effect (prevents fire damage for 40 seconds)
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 1));
    }
}
