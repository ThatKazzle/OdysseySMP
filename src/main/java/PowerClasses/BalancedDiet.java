package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class BalancedDiet extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();


    PotionEffect satEffect = new PotionEffect(PotionEffectType.SATURATION, 30 * 20, 1, false, false);
    PotionEffect hungerEffect = new PotionEffect(PotionEffectType.HUNGER, 10 * 20, 1, false, false);

    public BalancedDiet(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);

        if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "husbandry/balanced_diet") && !isOnCooldown(player.getUniqueId(), cooldowns)) {
            setCooldown(player.getUniqueId(), cooldowns, 60 * 3);

            player.addPotionEffect(satEffect);

            for (Player playerCheck : plugin.getServer().getOnlinePlayers()) {
                if (playerCheck != player && playerCheck.getLocation().distance(player.getLocation()) <= 25) {
                    playerCheck.addPotionEffect(hungerEffect);

                    playerCheck.sendMessage(ChatColor.RED + player.getName() + " has given you " + ChatColor.AQUA + "Hunger" + ChatColor.RED + "!");
                }
            }
        } else if (isOnCooldown(player.getUniqueId(), cooldowns)) {
            cantUsePowerMessage(player, cooldowns, "Stored Energy");
        }
    }
}
