package PowerClasses;

import kazzleinc.simples5.FollowParticle;
import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class EventPowerOne extends ParentPowerClass implements Listener {

    public final HashMap<UUID, Long> auralBarrageCooldowns = new HashMap<>();
    public final HashMap<UUID, Long> powerSlamCooldowns = new HashMap<>();

    public EventPowerOne(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Power Slam: " + getCooldownTimeLeft(player.getUniqueId(), powerSlamCooldowns);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (!player.isSneaking()) {
            auralBarrage(player);
        } else {
            PowerSlam(player);
        }
    }

    public void equipAnimation(Player player) {
        ItemDisplay display = player.getWorld().spawn(player.getLocation(), ItemDisplay.class);

        display.setItemStack(new ItemStack(Material.AMETHYST_SHARD));
    }

    public void auralBarrage(Player player) {
        if (!isOnCooldown(player.getUniqueId(), auralBarrageCooldowns)) {
            BukkitTask particle = new FollowParticle(player.getLocation(), player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(2)), plugin).runTaskTimer(plugin, 0, 0L);
            setCooldown(player.getUniqueId(), auralBarrageCooldowns, 0);
        }
    }

    public void PowerSlam(Player player) {
        if (!isOnCooldown(player.getUniqueId(), powerSlamCooldowns)) {
            setCooldown(player.getUniqueId(), powerSlamCooldowns, 60);
            EnderPearl pearl = player.getWorld().spawn(player.getEyeLocation(), EnderPearl.class);
            pearl.setShooter(player);
            pearl.setHasBeenShot(true);

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1.f, 1.f);

            pearl.setVelocity(player.getEyeLocation().getDirection().multiply(2));
        }
    }
}
