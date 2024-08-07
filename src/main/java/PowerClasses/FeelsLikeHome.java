package PowerClasses;

import com.comphenix.protocol.PacketType;
import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class FeelsLikeHome extends ParentPowerClass implements Listener {
    public HashMap<UUID, Long> cooldowns = new HashMap<>();
    public HashMap<UUID, Long> damageShareCooldowns = new HashMap<>();

    public HashMap<UUID, UUID> sharedDamageMap = new HashMap<>();


    PotionEffect regenPot = new PotionEffect(PotionEffectType.REGENERATION, 20 * 15, 1, false, false, true);
    PotionEffect strengthPot = new PotionEffect(PotionEffectType.STRENGTH, 20 * 15, 1, false, false, true);
    PotionEffect fireResPot = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, true);

    SimpleS5 plugin;

    public FeelsLikeHome(SimpleS5 plugin) {
        super(plugin);
        this.plugin = plugin;
    }



    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);

        if (hasPower(player, "nether/ride_strider_in_overworld_lava")) {
            if (player.isSneaking()) {
                blazedAction(player);
            } else {
                damageSharingAction(player);
            }
        }


    }
    public void blazedAction(Player player) {
            if (!isOnCooldown(player.getUniqueId(), cooldowns)) {

                player.setVisualFire(true);

                setCooldown(player.getUniqueId(), cooldowns, 120);

                player.addPotionEffect(regenPot);
                player.addPotionEffect(strengthPot);

                plugin.getServer().getWorld("world").playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.f, 1.f);
                player.sendMessage(ChatColor.RED + "You are ANGWY!");

                ParticleUtils.createParticleRing(player.getLocation().add(new Vector(0, 0.1, 0)), 4, 20, Particle.DUST, Color.fromRGB(255, 174, 66), 1);

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    player.setVisualFire(false);
                }, 10 * 20);

            } else if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                cantUsePowerMessage(player, cooldowns, "Blazed");
            }
    }

    public void damageSharingAction(Player player) {
        if (!isOnCooldown(player.getUniqueId(), damageShareCooldowns)) {
            RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 45, entity -> entity != player);

            if (result != null && result.getHitEntity() != null && result.getHitEntity() instanceof Player) {
                setCooldown(player.getUniqueId(), damageShareCooldowns, 60 * 4);

                Player hitPlayer = (Player) result.getHitEntity();

                sharedDamageMap.put(player.getUniqueId(), hitPlayer.getUniqueId());

                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1.f, 1.f);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You now share damage with " + ChatColor.BOLD + hitPlayer.getName());

                hitPlayer.playSound(hitPlayer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1.f, 1.f);
                hitPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "You now share damage with " + ChatColor.BOLD + player.getName());

                //particle stuff
                BukkitTask particleTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        ParticleUtils.createParticleLine(player.getLocation(), hitPlayer.getLocation(), 5, new Particle.DustOptions(Color.RED, 1));
                    }
                }.runTaskTimer(plugin, 0, 2);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sharedDamageMap.remove(player.getUniqueId());
                        particleTask.cancel();
                    }
                }.runTaskLater(plugin, 20 * 10);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damagedPlayer = (Player) event.getEntity();

            if (hasPower(damagedPlayer, "nether/ride_strider_in_overworld_lava") && sharedDamageMap.containsKey(damagedPlayer.getUniqueId())) {
                damager.damage(event.getDamage());
            }
        }
    }

    //make 15% chance to hit player with fire

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();;
        if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "ride_strider_in_overworld_lava")) {
            player.addPotionEffect(fireResPot);
        }
    }

    public void removeFireResistance(Player player) {
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
    }


}
