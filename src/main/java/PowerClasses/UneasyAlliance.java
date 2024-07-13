package PowerClasses;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UneasyAlliance extends ParentPowerClass implements Listener {

    private final Set<UUID> invisiblePlayers = new HashSet<>();
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public UneasyAlliance(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "nether/uneasy_alliance")) {
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                cantUsePowerMessage(player, cooldowns, "Invisibility");
            } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {

                makePlayerInvisible(player);
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                    makePlayerVisible(player);
                }, 15 * 20);

                setCooldown(player.getUniqueId(), cooldowns, 5 * 60);
            }
        }

    }

    public void registerInvisListener() {
       plugin.protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int entityId = event.getPacket().getIntegers().read(0);
                Player target = null;

                // Find the player by entity ID
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getEntityId() == entityId) {
                        target = player;
                        break;
                    }
                }

                // Check if the target player is invisible
                if (target != null && isInvisible(target)) {
                    // Cancel the packet to hide the armor
                    event.setCancelled(true);
                }
            }
        });
    }

    public void makePlayerInvisible(Player player) {
        invisiblePlayers.add(player.getUniqueId());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.hidePlayer(plugin, player);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 1.5f);
        player.sendActionBar(ChatColor.GREEN + "You are now " + ChatColor.RED + "completely invisible" + ChatColor.GREEN + ".");
    }

    public void makePlayerVisible(Player player) {
        invisiblePlayers.remove(player.getUniqueId());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.7f);
        player.sendActionBar(ChatColor.GREEN + "You are no longer " + ChatColor.RED + "completely invisible" + ChatColor.GREEN + ".");
    }

    public boolean isInvisible(Player player) {
        return invisiblePlayers.contains(player.getUniqueId());
    }
}
