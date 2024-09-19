package PowerClasses;

import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import kazzleinc.simples5.TrimUtils;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.UUID;

public class WithOurPowersCombined extends ParentPowerClass implements Listener {
    public HashMap<UUID, Long> cooldowns = new HashMap<>();

    public HashMap<UUID, Long>  mimicCooldowns = new HashMap<>();
    public HashMap<UUID, String> playerStoredPower = new HashMap<>();

    public HashMap<UUID, UUID> playerStoleFromPlayer = new HashMap<>();

    public HashMap<UUID, Long>  tornadoCooldowns = new HashMap<>();

    public HashMap<UUID, Long>  playerGetsPowerBackTime = new HashMap<>();

    private TrimUtils trimUtils = new TrimUtils();

    public WithOurPowersCombined(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (!player.isSneaking()) {
            tornadoAction(player);
        } else {
            //mimicAction(player);
            BlockDisplay blockDisplay = (BlockDisplay) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.BLOCK_DISPLAY);

            blockDisplay.setDisplayHeight(2);
            blockDisplay.setBlock(Material.WHITE_CONCRETE.createBlockData());
            new BukkitRunnable() {
                @Override
                public void run() {
                    blockDisplay.setTransformation(new Transformation(player.getLocation().toVector().toVector3f(), new Quaternionf(), new Vector(1, 2, 1).toVector3f(), new Quaternionf()));
                }
            }.runTaskTimer(plugin, 0, 0);
        }

    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Mimic: " + getCooldownTimeLeft(player.getUniqueId(), mimicCooldowns);
    }

    public void tornadoAction(Player player) {
        if (!isOnCooldown(player.getUniqueId(), tornadoCooldowns)) {
            setCooldown(player.getUniqueId(), tornadoCooldowns, 60);

            new BukkitRunnable() {
                Vector location = player.getLocation().toVector();
                Vector direction = player.getEyeLocation().getDirection().normalize();

                double radius = 1;
                double distance = 1;
                @Override
                public void run() {
                    Vector newLocation = location.clone().add(direction.clone().multiply(distance));
                    //location = location.add(location.multiply(distance));
                    radius = 1;
                    for (double y = -2; y < 3; y+= 0.1) {
                        ParticleUtils.createParticleRing(newLocation.clone().toLocation(player.getWorld()).add(new Vector(0, y, 0)), radius, (int) (radius * 8), Particle.SMALL_GUST, Color.GRAY, 1);

                        radius += 0.04;
                    }
                    distance += 0.4;

                    if (distance > 20) {
                        this.cancel();
                    }

                    for (Player playerCheck : SimpleS5.getPlayersInRange(newLocation.toLocation(player.getWorld()), 10)) {
                        double distanceToCenter = playerCheck.getLocation().distance(newLocation.toLocation(player.getWorld()));
                        Vector direction = newLocation.clone().add(new Vector(0, 1.5, 0)).subtract(playerCheck.getLocation().toVector()).normalize();


                        if (playerCheck.getWorld() == player.getWorld() && playerCheck != player) {
                            playerCheck.setVelocity(playerCheck.getVelocity().add(direction.clone().multiply(0.1)));
//                        if (distanceToCenter < 0.6) {
//                            playerCheck.setVelocity(playerCheck.getVelocity().add(direction.multiply(-0.09)));
//                        }
                            direction = null;
                        }
                    }
                }
            }.runTaskTimer(plugin, 0, 0);
        }
    }

    public void stealerAction(Player player) {
        RayTraceResult result = plugin.getServer().getWorld("world").rayTraceEntities(player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(1.3)), player.getEyeLocation().getDirection().normalize(), 6);

        if (result.getHitEntity() == null) return;

        ParticleUtils.createParticleRing(result.getHitPosition().toLocation(player.getWorld()), 1, 20, Particle.DUST, Color.RED, 1);

        //checks to make sure everything lines up right
        if (result.getHitEntity() == null) return;

        if (!(result.getHitEntity() instanceof Player)) return;

        Player beforeConversion = ((Player) result.getHitEntity());

        Player targetPlayer = Bukkit.getPlayer(plugin.provider.getInfo(beforeConversion).getName());
        String targetPlayerName = plugin.provider.getInfo(beforeConversion).getName();

        if (plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/froglights")) {
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                cantUsePowerMessage(player, cooldowns, "Odyssey Stealer");
            } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
                if (result.getHitEntity() != null) {

                    setCooldown(player.getUniqueId(), cooldowns, 60 * 10);

                    String targetPlayerEnabledKey;

                    String targetPlayerFormattedKey;

                    if (plugin.getPlayerPowersList(targetPlayer).get(plugin.getConfig().getInt("players." + targetPlayerName + ".mode")) != null) {
                        targetPlayerEnabledKey = plugin.getPlayerPowersList(targetPlayer).get(plugin.getConfig().getInt("players." + targetPlayerName + ".mode"));

                        targetPlayerFormattedKey = plugin.getAdvancementNameFormattedFromUnformattedString(targetPlayerEnabledKey);
                    } else {
                        player.sendMessage("That player doesn't have a power you can steal!");
                        return;
                    }

                    plugin.getConfig().set("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/froglights", false);
                    plugin.getConfig().set("players." + targetPlayerName + ".powers." + targetPlayerEnabledKey, false);

                    playerStoredPower.put(beforeConversion.getUniqueId(), targetPlayerEnabledKey);
                    playerStoleFromPlayer.put(player.getUniqueId(), beforeConversion.getUniqueId());

                    plugin.getConfig().set("players." + targetPlayerName + ".powers." + "player/power_stolen", true);

                    plugin.getConfig().set("players." + plugin.provider.getInfo(player).getName() + ".powers." + targetPlayerEnabledKey, true);

                    player.sendMessage(ChatColor.RED + "You stole " + ChatColor.AQUA + targetPlayerFormattedKey + ChatColor.RED + " from " + ChatColor.AQUA + targetPlayerName + "!");
                    targetPlayer.sendMessage(ChatColor.AQUA + plugin.provider.getInfo(player).getName() + ChatColor.RED + " stole " + ChatColor.AQUA + targetPlayerFormattedKey + "!");

                    setCooldown(targetPlayer.getUniqueId(), playerGetsPowerBackTime, 60 * 5);

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        plugin.getConfig().set("players." + plugin.provider.getInfo(player).getName() + ".powers." + targetPlayerEnabledKey, false);
                        plugin.getConfig().set("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/froglights", true);

                        plugin.getConfig().set("players." + targetPlayerName + ".powers." + "player/power_stolen", false);
                        plugin.getConfig().set("players." + targetPlayerName + ".powers." + targetPlayerEnabledKey, true);
                        playerStoredPower.remove(beforeConversion.getUniqueId());
                        playerStoleFromPlayer.remove(player.getUniqueId());

                        player.sendMessage(ChatColor.GREEN + "you lost your stolen power, and it has been given back to them.");
                        targetPlayer.sendMessage(ChatColor.GREEN + "You have been given your power back.");
                    }, 20 * (60 * 5));
                } else {
                    player.sendMessage(ChatColor.RED + "You didn't hit a player!");
                }
            }
        }
    }

    PotionEffect invisPot = new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 10, 0, false, false, false);

    public void mimicAction(Player player) {

    }
}
