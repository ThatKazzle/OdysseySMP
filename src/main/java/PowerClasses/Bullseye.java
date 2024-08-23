package PowerClasses;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.PowerPotionItem;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Bullseye extends ParentPowerClass implements Listener {

    public final HashMap<UUID, Long> sonicBoomCooldowns = new HashMap<>();
    public final HashMap<UUID, Long> flashbangCooldowns = new HashMap<>();

    public Bullseye(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Flashbang: " + getCooldownTimeLeft(player.getUniqueId(), flashbangCooldowns);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (!player.isSneaking()) {
            sonicBoomAction(player);
        } else {
            wardenHiredHelpAction(player);
        }
    }

    public void equipAnimation(Player player) {
        ItemDisplay display = player.getWorld().spawn(player.getLocation(), ItemDisplay.class);

        display.setItemStack(new ItemStack(Material.AMETHYST_SHARD));
    }

    public void sonicBoomAction(Player player) {
        if (!isOnCooldown(player.getUniqueId(), sonicBoomCooldowns)) {
            for (int i = 1; i < 20; i++) {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.f, 1.f);
                ParticleUtils.createWardenLine(player.getEyeLocation(), player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(30)), 10);
            }
        }
    }

    public void wardenHiredHelpAction(Player player) {

    }
}
