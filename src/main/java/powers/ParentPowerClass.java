package powers;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
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

    public abstract void action(Player player);

    /**
     * Called once when the plugin is disabled (server stop or /reload). Override it to undo
     * anything the power changed for a limited time, such as blocks, summoned mobs or hidden
     * players, because the tasks that would normally undo it are cancelled with the plugin.
     */
    public void onShutdown() {
    }

    public boolean isOnCooldown(UUID playerId, HashMap<UUID, Long> map) {
        return map.containsKey(playerId) && map.get(playerId) > System.currentTimeMillis();
    }

    public boolean hasPower(Player player, String powerKey) {
        return plugin.powerStore.hasPower(player, powerKey);
    }

    /**
     * Clears every cooldown this power keeps for the player: every {@code Map<UUID, Long>} field
     * declared on the power's class, except ones marked {@link NotACooldown}. New cooldown maps
     * are picked up automatically, so /rc can't fall out of date.
     */
    public void resetCooldowns(UUID playerId) {
        for (Class<?> type = getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                if (!java.util.Map.class.isAssignableFrom(field.getType())) continue;
                if (field.isAnnotationPresent(NotACooldown.class)) continue;
                if (!(field.getGenericType() instanceof java.lang.reflect.ParameterizedType generic)) continue;

                java.lang.reflect.Type[] args = generic.getActualTypeArguments();
                if (args.length != 2 || args[0] != UUID.class || args[1] != Long.class) continue;

                try {
                    field.setAccessible(true);
                    Object map = field.get(this);
                    if (map != null) ((java.util.Map<?, ?>) map).remove(playerId);
                } catch (IllegalAccessException e) {
                    plugin.getLogger().warning("Could not reset " + type.getSimpleName() + "." + field.getName() + ": " + e.getMessage());
                }
            }
        }
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

    public void makePlayerInvisible(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.hidePlayer(plugin, player);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 1.5f);
        player.sendMessage(ChatColor.GREEN + "You are now " + ChatColor.RED + "completely invisible" + ChatColor.GREEN + ".");
    }

    public void makePlayerVisible(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.7f);
        player.sendMessage(ChatColor.GREEN + "You are no longer " + ChatColor.RED + "completely invisible" + ChatColor.GREEN + ".");
    }
}
