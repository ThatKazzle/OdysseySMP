package PowerClasses;

import com.comphenix.protocol.PacketType;
import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.RandomUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.entity.Entity;
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


    PotionEffect regenPot = new PotionEffect(PotionEffectType.REGENERATION, 20 * 15, 2, false, false, true);
    PotionEffect strengthPot = new PotionEffect(PotionEffectType.STRENGTH, 20 * 15, 1, false, false, true);
    PotionEffect fireResPot = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, true);

    SimpleS5 plugin;

    public FeelsLikeHome(SimpleS5 plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Damage Link: " + getCooldownTimeLeft(player.getUniqueId(), damageShareCooldowns);
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
            RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 6, entity -> entity != player);

            if (result != null && result.getHitEntity() != null && result.getHitEntity() instanceof Player) {
                setCooldown(player.getUniqueId(), damageShareCooldowns, 60 * 4);

                Player hitPlayer = (Player) result.getHitEntity();

                sharedDamageMap.put(player.getUniqueId(), hitPlayer.getUniqueId());

                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1.f, 1.f);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You now share damage with " + ChatColor.BOLD + hitPlayer.getName());

                hitPlayer.playSound(hitPlayer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1.f, 1.f);
                hitPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "You now share damage with " + ChatColor.BOLD + plugin.provider.getInfo(player).getName());

                //particle stuff
                BukkitTask particleTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        hitPlayer.setHealth(player.getHealth());
                        ParticleUtils.createParticleLine(player.getLocation().add(new Vector(0, 1, 0)), hitPlayer.getLocation().add(new Vector(0, 1, 0)), (int) player.getLocation().distance(hitPlayer.getLocation()) * 3, new Particle.DustOptions(Color.PURPLE, 1));
                        if (player.isDead()) {
                            this.cancel();
                            hitPlayer.setKiller(player);
                            hitPlayer.setHealth(0);
                        }
                    }
                }.runTaskTimer(plugin, 0, 2);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sharedDamageMap.remove(player.getUniqueId());
                        particleTask.cancel();
                    }
                }.runTaskLater(plugin, 20 * 10);
            } else {
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.f, 1.f);
                player.sendMessage(ChatColor.RED + "You didn't hit anyone.");
            }
        }
    }

//    @EventHandler
//    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
//        if (event.getEntity() instanceof Player) {
//            Player damager = (Player) event.getDamager();
//            Player damagedPlayer = (Player) event.getEntity();
//
//            if (event.getDamager() instanceof Player) {
//                if (hasPower(damager, "nether/ride_strider_in_overworld_lava") && RandomUtils.getRandomIntInRange(1, 4) == 4 && damager.isVisualFire()) {
//                    damagedPlayer.setFireTicks(20 * 5);
//                }
//            }
//            if (hasPower(damagedPlayer, "nether/ride_strider_in_overworld_lava") && sharedDamageMap.containsKey(damagedPlayer.getUniqueId())) {
//                Bukkit.getPlayer(sharedDamageMap.get(damagedPlayer.getUniqueId())).damage(event.getFinalDamage());
//                Bukkit.getPlayer(sharedDamageMap.get(damagedPlayer.getUniqueId())).setNoDamageTicks(0);
//            }
//        }
//
//    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();;
        if (plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "ride_strider_in_overworld_lava")) {
            player.addPotionEffect(fireResPot);
        }
    }

    public void removeFireResistance(Player player) {
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
    }


}
