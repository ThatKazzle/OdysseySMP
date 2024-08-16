package PowerClasses;

import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BalancedDiet extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public final HashMap<UUID, Long> melonCooldowns = new HashMap<>();

    public final ArrayList<UUID> doesSiphon = new ArrayList<>();


    private PotionEffect satEffect = new PotionEffect(PotionEffectType.SATURATION, 30 * 20, 1, false, false, true);
    private PotionEffect hungerEffect = new PotionEffect(PotionEffectType.HUNGER, 10 * 20, 59, false, false, true);
    private PotionEffect nauseaEffect = new PotionEffect(PotionEffectType.HUNGER, 10 * 20, 1, false, false, true);

    public BalancedDiet(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        fattyAction(player);
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Juggernaut: " + getCooldownTimeLeft(player.getUniqueId(), melonCooldowns);
    }

    public void fattyAction(Player player) {
        if (plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/balanced_diet") && !isOnCooldown(player.getUniqueId(), cooldowns)) {
            setCooldown(player.getUniqueId(), cooldowns, 60 * 3);

            player.addPotionEffect(satEffect);

            for (Player playerCheck : plugin.getServer().getOnlinePlayers()) {
                if (playerCheck != player && playerCheck.getWorld() == player.getWorld() && playerCheck.getLocation().distance(player.getLocation()) <= 25) {
                    playerCheck.addPotionEffect(hungerEffect);
                    playerCheck.addPotionEffect(nauseaEffect);

                    playerCheck.sendMessage(ChatColor.RED + plugin.provider.getInfo(player).getName() + " has given you " + ChatColor.AQUA + "Hunger & Nausea" + ChatColor.RED + "!");
                }
            }
        } else if (isOnCooldown(player.getUniqueId(), cooldowns)) {
            cantUsePowerMessage(player, cooldowns, "Stored Energy");
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (hasPower(player, "husbandry/balanced_diet")) {
            if (event.getItem().getType() == Material.GLISTERING_MELON_SLICE && !isOnCooldown(player.getUniqueId(), melonCooldowns)) {
                setCooldown(player.getUniqueId(), melonCooldowns, 90);

                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(5.0);
                player.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.2);
                player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).setBaseValue(3.5);

                ParticleUtils.createParticleSphere(player.getLocation(), 3, 50, Particle.DUST, Color.ORANGE, 2);
                new BukkitRunnable() {
                    Player checkPlayer = player;
                    @Override
                    public void run() {
                        AttributeInstance attackSpeed = checkPlayer.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                        AttributeInstance scale = checkPlayer.getAttribute(Attribute.GENERIC_SCALE);
                        AttributeInstance reach = checkPlayer.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE);

                        plugin.getLogger().info(ChatColor.GREEN + "attrRemover ran once");


                        if (checkPlayer.isOnline()) {
                            if (attackSpeed.getBaseValue() != 4.0) {
                                attackSpeed.setBaseValue(4.0);
                                plugin.getLogger().info(ChatColor.RED + "Updated attack speed");
                            }

                            if (scale.getBaseValue() != 1.0) {
                                scale.setBaseValue(1.0);
                                plugin.getLogger().info(ChatColor.RED + "Updated scale");
                            }

                            if (reach.getBaseValue() != 3.0) {
                                reach.setBaseValue(3.0);
                                plugin.getLogger().info(ChatColor.RED + "Updated reach");
                            }

                            ParticleUtils.createParticleSphere(checkPlayer.getLocation(), 3, 50, Particle.DUST, Color.ORANGE, 2);

                            plugin.getLogger().info("Everything goes back to normal idiot.");
                            this.cancel();
                        }
                    }
                }.runTaskLater(plugin, 15 * 20);
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the player is holding the custom edible item (glistering melon)
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.GLISTERING_MELON_SLICE && hasPower(event.getPlayer(), "husbandry/balanced_diet")) {
            event.setCancelled(true);  // Cancel the default interaction

            // Simulate eating the item
            PlayerItemConsumeEvent consumeEvent = new PlayerItemConsumeEvent(event.getPlayer(), item);
            plugin.getServer().getPluginManager().callEvent(consumeEvent);

            if (!consumeEvent.isCancelled()) {
                // Apply effects to the player (e.g., restore hunger and health)
                event.getPlayer().setFoodLevel(Math.min(event.getPlayer().getFoodLevel() + 4, 20));  // Restore 4 hunger

                // Remove one glistering melon from the player's hand
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    event.getPlayer().getInventory().setItemInMainHand(null);  // Clear the item if only one was left
                }

                event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), "entity.generic.eat", 1.0f, 1.0f);
            }
        }
    }
}
