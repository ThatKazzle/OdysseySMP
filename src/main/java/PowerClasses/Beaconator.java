package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Beaconator extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    private final List<PotionEffectType> potionTypes = Arrays.asList(PotionEffectType.SPEED, PotionEffectType.REGENERATION, PotionEffectType.HASTE);

    SimpleS5 plugin;

    public Beaconator(SimpleS5 plugin) {
        super(plugin);
    }

    //speed 2, regen 2, haste 2

    //10 seconds long

    //3 minute cooldown

    @Override
    public void action(String playerName) {
        Player player = (Player) plugin.getServer().getPlayer(playerName);
        if (isOnCooldown(player.getUniqueId(), cooldowns)) {
            cantUsePowerMessage(player, cooldowns, "Beaconator");
        } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 1.f);
            player.sendMessage(ChatColor.GREEN + "You gained " + ChatColor.AQUA + plugin.titleCaseString(addRandomPotionEffects(player).getName().toLowerCase()));
        }
    }

    public PotionEffectType addRandomPotionEffects(Player player) {
        PotionEffectType randomElement = getRandomElement(potionTypes);

        player.addPotionEffect(new PotionEffect(randomElement, 20 * 20, 1, false, false, true));

        return randomElement;
    }

    public static PotionEffectType getRandomElement(List<PotionEffectType> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(randomIndex);
    }
}
