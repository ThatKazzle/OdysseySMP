package PowerClasses;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import it.unimi.dsi.fastutil.Hash;
import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UneasyAlliance extends ParentPowerClass implements Listener {

    private final Set<UUID> invisiblePlayers = new HashSet<>();

    public final HashMap<UUID, Long> cooldowns = new HashMap<>();
    public final HashMap<UUID, Long> freezeCooldowns = new HashMap<>();

    public final HashMap<UUID, Boolean> isFrozen = new HashMap<>();

    public UneasyAlliance(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);

        if (!player.isSneaking()) {
            invisAction(player);
        } else {
            freezeAction(player);
        }

    }

    @EventHandler
    public void onPlayerJumpEvent(PlayerJumpEvent event) {
        Player player = event.getPlayer();

        if (isFrozen.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (isFrozen.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Freeze Ray: " + getCooldownTimeLeft(player.getUniqueId(), freezeCooldowns);
    }

    private void invisAction(Player player) {
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

    private void freezeAction(Player player) {
        if (this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "nether/uneasy_alliance")) {
            if (!isOnCooldown(player.getUniqueId(), freezeCooldowns)) {

                RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 45, entity -> entity != player);

                if (result != null && result.getHitEntity() instanceof Player) {
                    setCooldown(player.getUniqueId(), freezeCooldowns, 60);

                    Player hitPlayer = (Player) result.getHitEntity();

                    isFrozen.put(hitPlayer.getUniqueId(), true);
                    hitPlayer.setAllowFlight(true);

                    BukkitTask soundRunner = new BukkitRunnable() {
                        @Override
                        public void run() {
                            //ParticleUtils.createParticleRing(hitPlayer.getEyeLocation(), 1.5, 40, Particle.DUST, Color.WHITE, 1);
                            hitPlayer.getWorld().playSound(hitPlayer.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.f, 1.f);

                            hitPlayer.setSaturation(0);
                            hitPlayer.damage(0.000001);

                            if (hitPlayer.getHealth() - 0.75 < 0) {
                                hitPlayer.setHealth(0);
                            } else {
                                hitPlayer.setHealth(hitPlayer.getHealth());
                            }
                        }
                    }.runTaskTimer(plugin, 0, 20);

                    BukkitTask particleRunner = new BukkitRunnable() {
                        int freeze = 0;
                        @Override
                        public void run() {

                            freeze += 1;
                            double freezeDiv = (double) freeze / 100;
                            //player.sendMessage(String.valueOf(freezeDiv));

                            ParticleUtils.createParticleRing(getInterpolatedLocation(hitPlayer.getLocation(), hitPlayer.getEyeLocation(), freezeDiv), 1.5, 30, Particle.DUST, Color.WHITE, 1);
                            ParticleUtils.createParticleRing(getInterpolatedLocation(hitPlayer.getLocation(), hitPlayer.getEyeLocation(), 1 - freezeDiv), 1.5, 30, Particle.DUST, Color.WHITE, 1);
                        }
                    }.runTaskTimer(plugin, 0, 1);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage("unfroze " + hitPlayer.getName());
                            isFrozen.remove(hitPlayer.getUniqueId());
                            hitPlayer.setAllowFlight(false);
                            soundRunner.cancel();
                            particleRunner.cancel();
                            this.cancel();
                        }
                    }.runTaskLater(plugin, 20 * 5);
                } else {
                    player.sendMessage(ChatColor.RED + "No players were hit.");
                }
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
        plugin.sendJukeboxMessage(player, ChatColor.GREEN + "You are now " + ChatColor.RED + "completely invisible" + ChatColor.GREEN + ".");
    }

    public void makePlayerVisible(Player player) {
        invisiblePlayers.remove(player.getUniqueId());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.7f);
        plugin.sendJukeboxMessage(player, ChatColor.GREEN + "You are no longer " + ChatColor.RED + "completely invisible" + ChatColor.GREEN + ".");
    }

    public boolean isInvisible(Player player) {
        return invisiblePlayers.contains(player.getUniqueId());
    }

    public Location getInterpolatedLocation(Location start, Location end, double t) {
        if (t < 0.0 || t > 1.0) {
            throw new IllegalArgumentException("t must be between 0 and 1");
        }

        double x = lerp(start.getX(), end.getX(), t);
        double y = lerp(start.getY(), end.getY(), t);
        double z = lerp(start.getZ(), end.getZ(), t);

        // Return the new Location object with interpolated coordinates
        return new Location(start.getWorld(), x, y, z);
    }

    private double lerp(double start, double end, double t) {
        return (1 - t) * start + t * end;
    }
}
