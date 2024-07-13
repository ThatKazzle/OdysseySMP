package PowerClasses;

import kazzleinc.simples5.RandomUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class HowDidWeGetHere extends ParentPowerClass implements Listener {

    List<PotionEffectType> potionTypes = Arrays.asList(PotionEffectType.BLINDNESS, PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.WITHER, PotionEffectType.NAUSEA, PotionEffectType.HUNGER, PotionEffectType.DARKNESS, PotionEffectType.MINING_FATIGUE, PotionEffectType.SLOW_FALLING);

    List<PotionEffectType> posPotionTypes = Arrays.asList(PotionEffectType.ABSORPTION, PotionEffectType.REGENERATION, PotionEffectType.SATURATION, PotionEffectType.STRENGTH, PotionEffectType.SPEED, PotionEffectType.RESISTANCE);
    //private static final List<String> AUTOFILL_ARGS_1 = Arrays.asList("withdraw", "powers");

    public HowDidWeGetHere(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {

    }

    @EventHandler
    public void onEntityDamageEntityEvent(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player hitPlayer = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            if (this.plugin.getConfig().getBoolean("players." + damager.getName() + ".powers." + "nether/all_effects")) {
                if (RandomUtils.getRandomIntInRange(0, 20) > 19) {
                    PotionEffectType potion = getRandomElement(potionTypes);

                    hitPlayer.addPotionEffect(new PotionEffect(potion, 20 * 10, 1, false, true));

                    strikeLightningEffect(hitPlayer, hitPlayer.getLocation());

                    hitPlayer.playSound(hitPlayer, Sound.BLOCK_ANVIL_BREAK, 1.f, 1.f);
                    hitPlayer.sendMessage(ChatColor.AQUA + damager.getName() + ChatColor.GREEN + " has given you " + ChatColor.YELLOW + plugin.titleCaseString(potion.getName().replace("_", " ").toLowerCase()) + ChatColor.GREEN + "!");
                    damager.playSound(hitPlayer, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.2f, 1.f);
                    damager.sendMessage(ChatColor.GREEN + "You have inflicted " + ChatColor.AQUA + hitPlayer.getDisplayName() + ChatColor.GREEN + " with " + ChatColor.YELLOW + plugin.titleCaseString(potion.getName().toLowerCase()) + ChatColor.GREEN + "!");
                }

                if (RandomUtils.getRandomIntInRange(0, 30) > 29) {
                    PotionEffectType potion = getRandomElement(posPotionTypes);

                    damager.addPotionEffect(new PotionEffect(potion, 20 * 10, 1, false, true, true));

                    damager.playSound(hitPlayer, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
                    damager.sendMessage(ChatColor.GREEN + "You have been given " + ChatColor.YELLOW + plugin.titleCaseString(potion.getName().replace("_", " ").toLowerCase()) + ChatColor.GREEN + "!");
                }
            }

        }
    }

    public void strikeLightningEffect(Player player, Location location) {
        World world = player.getWorld();
        world.strikeLightningEffect(location);
    }

    public static PotionEffectType getRandomElement(List<PotionEffectType> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(randomIndex);
    }
}
