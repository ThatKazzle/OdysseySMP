package PowerClasses;

import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class Bullseye extends ParentPowerClass implements Listener {

    public final HashMap<UUID, Long> sonicBoomCooldowns = new HashMap<>();
    public final HashMap<UUID, Long> pearlCooldowns = new HashMap<>();

    public Bullseye(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Flashbang: " + getCooldownTimeLeft(player.getUniqueId(), pearlCooldowns);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (!player.isSneaking()) {
            sonicBoomAction(player);
        } else {
            pearlAction(player);
        }
    }

    public void equipAnimation(Player player) {
        ItemDisplay display = player.getWorld().spawn(player.getLocation(), ItemDisplay.class);

        display.setItemStack(new ItemStack(Material.AMETHYST_SHARD));
    }

    public void sonicBoomAction(Player player) {
        if (!isOnCooldown(player.getUniqueId(), sonicBoomCooldowns)) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.f, 1.f);
            ParticleUtils.createWardenLine(player.getEyeLocation(), player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(30)), 10, player);
            setCooldown(player.getUniqueId(), sonicBoomCooldowns, (60 * 2) + 30);
        }
    }

    public void pearlAction(Player player) {
        if (!isOnCooldown(player.getUniqueId(), pearlCooldowns)) {
            setCooldown(player.getUniqueId(), pearlCooldowns, 60);
            EnderPearl pearl = player.getWorld().spawn(player.getEyeLocation(), EnderPearl.class);
            pearl.setShooter(player);
            pearl.setHasBeenShot(true);

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1.f, 1.f);

            pearl.setVelocity(player.getEyeLocation().getDirection().multiply(2));
        }
    }
}
