package PowerClasses;

import kazzleinc.simples5.FollowParticle;
import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CompleteCatalogue extends ParentPowerClass implements Listener {

    public HashMap<UUID, Long> cooldowns = new HashMap<>();
    public HashMap<UUID, Long> effectStealerCooldown = new HashMap<>();
    public final HashMap<UUID, Long> auralBarrageCooldowns = new HashMap<>();

    List<PotionEffectType> negPotionTypes = Arrays.asList(PotionEffectType.BLINDNESS, PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.WITHER, PotionEffectType.NAUSEA, PotionEffectType.HUNGER, PotionEffectType.DARKNESS, PotionEffectType.MINING_FATIGUE, PotionEffectType.SLOW_FALLING);

    SimpleS5 plugin;
    public CompleteCatalogue(SimpleS5 plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player.isSneaking()) {
            effectStealerAction(Bukkit.getPlayer(playerName));
        } else {
            auralBarrage(player);
        }
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Potion Stealer: " + getCooldownTimeLeft(player.getUniqueId(), effectStealerCooldown);
    }

    public void startVisualHits() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (hasPower(player, "husbandry/complete_catalogue")) {
                        player.getWorld().spawnParticle(Particle.DUST, player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(6)), 2, new Particle.DustOptions(Color.AQUA, 2));
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 3);
    }

    public void effectStealerAction(Player player) {
        if (plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/complete_catalogue")) {
            if (!isOnCooldown(player.getUniqueId(), effectStealerCooldown)) {

                RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 6, entity -> entity != player);

                if (result != null && result.getHitEntity() != null && result.getHitEntity() instanceof Player) {
                    setCooldown(player.getUniqueId(), effectStealerCooldown, 60 * 5);
                    Player hitPlayer = (Player) result.getHitEntity();

                    if (!hitPlayer.getActivePotionEffects().isEmpty()) {
                        for (PotionEffect effect : hitPlayer.getActivePotionEffects()) {
                            player.addPotionEffect(effect);
                            hitPlayer.removePotionEffect(effect.getType());
                        }
                        hitPlayer.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.f, 1.f);

                        player.sendMessage(ChatColor.AQUA + "You stole " + ChatColor.GREEN + hitPlayer.getName() + "'s " + ChatColor.AQUA + "potions.");
                        hitPlayer.sendMessage(ChatColor.GREEN + plugin.provider.getInfo(player).getName() + ChatColor.AQUA + " stole your potions.");

                        ParticleUtils.createParticleRandomLine(player.getLocation(), hitPlayer.getLocation(), (int) player.getLocation().distance(hitPlayer.getLocation()) * 3, 2);
                    }
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.f, 1.f);
                    player.sendMessage("You didn't hit a player.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTakeDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/complete_catalogue") && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }
    }

    public void auralBarrage(Player player) {
        if (!isOnCooldown(player.getUniqueId(), auralBarrageCooldowns)) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.f, 1.f);
            for (int i = 0; i < 10; i++) {
                new FollowParticle(player.getEyeLocation(), player.getEyeLocation().getDirection(), player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(6)), player, plugin).runTaskTimer(plugin, 0, 0L);
            }
            setCooldown(player.getUniqueId(), auralBarrageCooldowns, 120);
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
