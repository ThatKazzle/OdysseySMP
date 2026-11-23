package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PowerStealerPlaceholder extends ParentPowerClass {

    public PowerStealerPlaceholder(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.RED + ChatColor.MAGIC + "asdf " + ChatColor.RESET + ChatColor.RED + "Unusable for " + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.RESET + ChatColor.RED + ChatColor.MAGIC + " asdf";
    }


    public static String getStolenPowerString() {
        return "" + ChatColor.RED + ChatColor.MAGIC + "asdf " + ChatColor.RESET + ChatColor.RED + "Unuseable Power" + ChatColor.MAGIC + " asdf";
    }

    @Override
    public void action(Player player) {
        // A WOPC victim holds this slot until their stolen power comes back.
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.f, 1.f);
        player.sendMessage(ChatColor.RED + "Your power has been stolen, you can't use this!");
    }
}
