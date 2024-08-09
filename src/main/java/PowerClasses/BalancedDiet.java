package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BalancedDiet extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public final HashMap<UUID, Long> carrotCooldowns = new HashMap<>();

    public final ArrayList<UUID> doesSiphon = new ArrayList<>();


    PotionEffect satEffect = new PotionEffect(PotionEffectType.SATURATION, 30 * 20, 1, false, false);
    PotionEffect hungerEffect = new PotionEffect(PotionEffectType.HUNGER, 10 * 20, 59, false, false);

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
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Juggernaut: " + getCooldownTimeLeft(player.getUniqueId(), carrotCooldowns);
    }

    public void fattyAction(Player player) {
        if (plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/balanced_diet") && !isOnCooldown(player.getUniqueId(), cooldowns)) {
            setCooldown(player.getUniqueId(), cooldowns, 60 * 3);

            player.addPotionEffect(satEffect);

            for (Player playerCheck : plugin.getServer().getOnlinePlayers()) {
                if (playerCheck != player && playerCheck.getLocation().distance(player.getLocation()) <= 25) {
                    playerCheck.addPotionEffect(hungerEffect);

                    playerCheck.sendMessage(ChatColor.RED + plugin.provider.getInfo(player).getName() + " has given you " + ChatColor.AQUA + "Hunger" + ChatColor.RED + "!");
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
            if (event.getItem().getType() == Material.GOLDEN_CARROT && !isOnCooldown(player.getUniqueId(), carrotCooldowns)) {
                setCooldown(player.getUniqueId(), carrotCooldowns, 90);

                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(5.0);
                player.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.2);
                player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).setBaseValue(3.5);

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

                            plugin.getLogger().info("Everything goes back to normal idiot.");
                            this.cancel();
                        }
                    }
                }.runTaskLater(plugin, 15 * 20);
            }
        }
    }
}
