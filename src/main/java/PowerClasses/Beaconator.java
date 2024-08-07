package PowerClasses;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Beaconator extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    private final List<PotionEffectType> potionTypes = Arrays.asList(PotionEffectType.SPEED, PotionEffectType.REGENERATION, PotionEffectType.HASTE);

    public Beaconator(SimpleS5 plugin) {
        super(plugin);
    }

    //speed 2, regen 2, haste 2

    //10 seconds long

    //3 minute cooldown

    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);

        randomEffectAction(player);
    }

    public void randomEffectAction(Player player) {
        if (isOnCooldown(player.getUniqueId(), cooldowns)) {
            cantUsePowerMessage(player, cooldowns, "Beaconator");
        } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
            setCooldown(player.getUniqueId(), cooldowns, 90);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 1.f);

            player.sendMessage(ChatColor.GREEN + "You gained " + ChatColor.AQUA + plugin.titleCaseString(addRandomPotionEffects(player).getName().toLowerCase()));
        }

        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(ChatColor.GOLD + "Odyssey Shard");
        item.setItemMeta(meta);

        player.getInventory().addItem(item);
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

    public String formatPotionName() {
        return "balls";
    }
}
