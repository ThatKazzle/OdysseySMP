package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
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

    List<PotionEffectType> negPotionTypes = Arrays.asList(PotionEffectType.BLINDNESS, PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.WITHER, PotionEffectType.NAUSEA, PotionEffectType.HUNGER, PotionEffectType.DARKNESS, PotionEffectType.MINING_FATIGUE, PotionEffectType.SLOW_FALLING);

    SimpleS5 plugin;
    public CompleteCatalogue(SimpleS5 plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void action(String playerName) {
        effectStealerAction(Bukkit.getPlayer(playerName));
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Potion Stealer: " + getCooldownTimeLeft(player.getUniqueId(), effectStealerCooldown);
    }

    public void effectStealerAction(Player player) {
        if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "husbandry/complete_catalogue")) {
            if (!isOnCooldown(player.getUniqueId(), effectStealerCooldown)) {

                RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 45, entity -> entity != player);

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
                        hitPlayer.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.AQUA + " stole your potions.");
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

            if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "husbandry/complete_catalogue") && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }
    }

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

    private void applyTotemEffects(Player player) {
        // Apply Absorption effect (4 additional health points for 5 seconds)
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 5, 1));

        // Apply Regeneration effect (heals 1 health point every 0.5 seconds for 45 seconds)
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 45, 1));

        // Apply Fire Resistance effect (prevents fire damage for 40 seconds)
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40 * 20, 0));
    }
}
