package PowerClasses;

import dev.iiahmed.disguise.*;
import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import kazzleinc.simples5.TrimUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class WithOurPowersCombined extends ParentPowerClass implements Listener {
    public HashMap<UUID, Long> cooldowns = new HashMap<>();

    public HashMap<UUID, Long>  mimicCooldowns = new HashMap<>();
    public HashMap<UUID, String> playerStoredPower = new HashMap<>();

    public HashMap<UUID, UUID> playerStoleFromPlayer = new HashMap<>();

    public HashMap<UUID, Long>  frogCooldowns = new HashMap<>();

    public HashMap<UUID, Long>  playerGetsPowerBackTime = new HashMap<>();

    private TrimUtils trimUtils = new TrimUtils();

    public WithOurPowersCombined(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (!player.isSneaking()) {
            frogTongueAction(player);
        } else {
            mimicAction(player);
        }

    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Mimic: " + getCooldownTimeLeft(player.getUniqueId(), mimicCooldowns);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player deadPlayer = event.getPlayer();

        if (hasPower(deadPlayer, "player/power_stolen")) {
            event.setCancelled(true);
            plugin.getConfig().set("players." + plugin.provider.getInfo(deadPlayer).getName() + ".powers." + "player/power_stolen", false);

            plugin.getConfig().set("players." + plugin.provider.getInfo(deadPlayer).getName() + ".powers." + playerStoredPower.get(deadPlayer.getUniqueId()), true);
            deadPlayer.setHealth(0);

            OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(playerStoleFromPlayer.get(deadPlayer.getUniqueId()));

            String otherPlayerName = otherPlayer.getName();

            plugin.getConfig().set("players." + otherPlayerName + ".powers." + playerStoredPower.get(deadPlayer.getUniqueId()), false);
            plugin.getConfig().set("players." + otherPlayerName + ".powers." + "husbandry/froglights", true);

            playerStoredPower.remove(deadPlayer.getUniqueId());
            playerStoleFromPlayer.remove(deadPlayer.getUniqueId());

            deadPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "The power that " + ChatColor.RED + otherPlayerName + ChatColor.LIGHT_PURPLE + " stole has been dropped at your location.");
            if (otherPlayer.isOnline()) ((Player) otherPlayer).sendMessage(ChatColor.RED + plugin.provider.getInfo(deadPlayer).getName() + ChatColor.LIGHT_PURPLE + " has died, and you have been given WOPC back.");
        }
    }

    public void frogTongueAction(Player player) {
        RayTraceResult result = plugin.getServer().getWorld("world").rayTraceEntities(player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(1.3)), player.getEyeLocation().getDirection().normalize(), 6);

        ParticleUtils.createParticleRing(result.getHitPosition().toLocation(player.getWorld()), 1, 20, Particle.DUST, Color.RED, 1);

        //checks to make sure everything lines up right
        if (result.getHitEntity() == null) return;

        if (!(result.getHitEntity() instanceof Player)) return;

        Player hitPlayer = (Player) result.getHitEntity();

        if (plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/froglights")) {
            if (!isOnCooldown(player.getUniqueId(), frogCooldowns) && result.getHitEntity() != null) {

                new BukkitRunnable() {
                    double i = 0;

                    Vector origVector = player.getLocation().toVector().clone();
                    @Override
                    public void run() {
                        Vector dir = player.getLocation().toVector().subtract(hitPlayer.getLocation().toVector()).normalize();
                        if (i < 1) {
                            i += 0.05;

                            hitPlayer.setVelocity(dir.multiply(0.6));

                            //hitPlayer.teleport(plugin.lerp(origVector, player.getLocation().toVector(), i).toLocation(player.getWorld()), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        } else if (i > 1) {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 3);

                setCooldown(player.getUniqueId(), frogCooldowns, 120);
            }
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
        if (plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/froglights")) {
            if (!isOnCooldown(player.getUniqueId(), mimicCooldowns)) {
                RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 45, entity -> entity != player);

                if (result != null && result.getHitEntity() != null && result.getHitEntity() instanceof Player) {
                    setCooldown(player.getUniqueId(), mimicCooldowns, (60 * 2) + 30);
                    Player hitPlayer = (Player) result.getHitEntity();

                    double playerHealth = player.getHealth();
                    float playerSat = player.getSaturation();
                    int satRegenRate = player.getSaturatedRegenRate();

                    Vector playerVelocity = player.getVelocity();
                    int activeSlot = player.getInventory().getHeldItemSlot();

                    plugin.provider.setNamePattern(Pattern.compile("^[a-zA-Z0-9_.§]{1,16}$"));

                    Disguise disguise = Disguise.builder()
                            .setName(ChatColor.RESET + hitPlayer.getName())
                            // you could as well use Disguise.Builder#setSkin(Skin)
                            // or even Disguise.Builder#setSkin(uuid)
                            // it's recommended to run this async since #setSkin from an online API will block the mainthread
                            .setSkin(SkinAPI.MOJANG, hitPlayer.getUniqueId())
                            .build();
                    DisguiseResponse response = plugin.provider.disguise(player, disguise);

                    player.setHealth(playerHealth);
                    player.setSaturation(playerSat);
                    player.setSaturatedRegenRate(satRegenRate);
                    player.getInventory().setHeldItemSlot(activeSlot);

                    player.teleport(hitPlayer);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            DisguiseManager.getProvider().undisguise(player);
                        }
                    }.runTaskLater(plugin, 20 * 120);
                }
            }
        }
    }
}
