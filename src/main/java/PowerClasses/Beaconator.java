package PowerClasses;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import kazzleinc.simples5.PlayerState;
import kazzleinc.simples5.SimpleS5;
import kazzleinc.simples5.SpecialParticleUtils;
import org.bukkit.*;
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
    public final HashMap<UUID, Long> rewindCooldwns = new HashMap<>();
    private final Map<UUID, LinkedHashMap<Long, PlayerState>> playerStates = new HashMap<>();

    private SpecialParticleUtils particleUtils;

    private static final int BUFFER_SIZE = 60;

    private final List<PotionEffectType> potionTypes = Arrays.asList(PotionEffectType.SPEED, PotionEffectType.REGENERATION, PotionEffectType.HASTE);

    public Beaconator(SimpleS5 plugin) {
        super(plugin);

        particleUtils = new SpecialParticleUtils(plugin);
    }

    //speed 2, regen 2, haste 2

    //10 seconds long

    //3 minute cooldown

    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (hasPower(player, "nether/create_full_beacon")) {
            if (player.isSneaking()) {
                rewindAction(player);
            } else {
                randomEffectAction(player);
            }
        }
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Run it Back: " + getCooldownTimeLeft(player.getUniqueId(), rewindCooldwns);
    }

    public void rewindAction(Player player) {
        PlayerState state = getPlayerState(player.getUniqueId(), 3000);

        if (!isOnCooldown(player.getUniqueId(), rewindCooldwns)) {
            setCooldown(player.getUniqueId(), rewindCooldwns, 60 * 5);

            state.apply(player);
        } else {

        }
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

    public void startTrackingPlayerStates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (hasPower(player, "nether/create_full_beacon")) {
                        UUID playerId = player.getUniqueId();
                        playerStates.putIfAbsent(playerId, new LinkedHashMap<Long, PlayerState>(BUFFER_SIZE + 1, 1, false) {
                            protected boolean removeEldestEntry(Map.Entry<Long, PlayerState> eldest) {
                                return size() > BUFFER_SIZE;
                            }
                        });

                        playerStates.get(playerId).put(currentTime, new PlayerState(player));

                        particleUtils.sendClientDustParticle(player, getPlayerState(playerId, 3000).getRewindLocation(), new Particle.DustOptions(Color.RED, 2f), 1);
                    }

                }
            }
        }.runTaskTimer(plugin, 1, 1); // Runs every tick (50ms)
    }

    public PlayerState getPlayerState(UUID playerId, long timeAgoInMillis) {
        LinkedHashMap<Long, PlayerState> states = playerStates.get(playerId);
        if (states == null) return null;

        long targetTime = System.currentTimeMillis() - timeAgoInMillis;
        PlayerState closestState = null;
        long closestTimeDiff = Long.MAX_VALUE;

        for (Map.Entry<Long, PlayerState> entry : states.entrySet()) {
            long timeDiff = Math.abs(entry.getKey() - targetTime);
            if (timeDiff < closestTimeDiff) {
                closestTimeDiff = timeDiff;
                closestState = entry.getValue();
            }
        }

        return closestState;
    }
}
