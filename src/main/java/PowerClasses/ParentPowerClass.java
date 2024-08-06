package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public abstract class ParentPowerClass {

    public SimpleS5 plugin;

    public BukkitTask cooldownDisplayRunnable;

    public ParentPowerClass(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    public abstract void action(String playerName);

    public boolean isOnCooldown(UUID playerId, HashMap<UUID, Long> map) {
        return map.containsKey(playerId) && map.get(playerId) > System.currentTimeMillis();
    }

    public boolean hasPower(Player player, String powerKey) {
        return plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + powerKey);
    }


    /**
     * Sets a cooldown on a list, making it easier to do multiple powers.
     *
     * @param playerId
     * @param map
     * @param cooldown MAKE SURE THIS IS IN SECONDS!
     */
    public void setCooldown(UUID playerId, HashMap<UUID, Long> map, int cooldown) {
        map.put(playerId, System.currentTimeMillis() + (cooldown * 1000));
    }

    public void cantUsePowerMessage(Player player, HashMap<UUID, Long> cooldown, String power) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.f, 1.f);

        String timeLeft = getCooldownTimeLeft(player.getUniqueId(), cooldown);

        player.sendActionBar(ChatColor.RED + "alr chill, you have " + timeLeft + "s left on your " + power + " ability.");
    }

    public String getCooldownTimeLeft(UUID playerId, HashMap<UUID, Long> cooldown) {
        if (cooldown.get(playerId) != null) {
            long timeLeft = (cooldown.get(playerId) - System.currentTimeMillis()) / 1000;

            long seconds = timeLeft % 60;
            long minutes = timeLeft / 60;

            return ChatColor.RED + formatCooldownTime(timeLeft);
        } else {
            return "" + ChatColor.GREEN + ChatColor.BOLD + "Ready!";
        }
    }

    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap);
    }

    public String formatCooldownTime(long totalSeconds) {
        //long totalSeconds = ticks / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (totalSeconds <= 0) {
            return "" + ChatColor.GREEN + ChatColor.BOLD + "Ready!";
        } else if (minutes > 0) {
            return "" + ChatColor.RED + minutes + "m " + seconds + "s";
        } else {
            return "" + ChatColor.RED + seconds + "s";
        }
    }
}
