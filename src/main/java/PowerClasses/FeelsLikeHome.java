package PowerClasses;

import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class FeelsLikeHome extends ParentPowerClass implements Listener {
    public HashMap<UUID, Long> cooldowns = new HashMap<>();

    PotionEffect regenPot = new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 1, false, false, true);
    PotionEffect strengthPot = new PotionEffect(PotionEffectType.STRENGTH, 20 * 10, 1, false, false, true);
    PotionEffect fireResPot = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, true);

    SimpleS5 plugin;

    public FeelsLikeHome(SimpleS5 plugin) {
        super(plugin);
        this.plugin = plugin;
    }



    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);

        if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "nether/ride_strider_in_overworld_lava")) {

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
    }

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
