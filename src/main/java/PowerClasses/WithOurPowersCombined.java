package PowerClasses;

import dev.iiahmed.disguise.*;
import kazzleinc.simples5.ParticleUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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

    public WithOurPowersCombined(SimpleS5 plugin) {
        super(plugin);
    }



    @Override
    public void action(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (player.isSneaking()) {
            stealerAction(player);
        } else {
            mimicAction(player);
        }

    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Mimic: " + getCooldownTimeLeft(player.getUniqueId(), mimicCooldowns);
    }

    public void stealerAction(Player player) {
        RayTraceResult result = plugin.getServer().getWorld("world").rayTraceEntities(player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(1.3)), player.getEyeLocation().getDirection().normalize(), 45);

        if (result.getHitEntity() == null) return;

        ParticleUtils.createParticleRing(result.getHitPosition().toLocation(player.getWorld()), 1, 20, Particle.DUST, Color.RED, 1);

        //checks to make sure everything lines up right
        if (result.getHitEntity() == null) return;

        if (!(result.getHitEntity() instanceof Player)) return;

        Player targetPlayer = ((Player) result.getHitEntity());

        if (plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/froglights")) {
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                cantUsePowerMessage(player, cooldowns, "Odyssey Stealer");
            } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
                if (result.getHitEntity() != null) {

                    setCooldown(player.getUniqueId(), cooldowns, 60 * 10);

                    String targetPlayerEnabledKey;

                    if (plugin.getPlayerPowersList(targetPlayer).get(plugin.getConfig().getInt("players." + targetPlayer.getName() + ".mode")) != null) {
                        targetPlayerEnabledKey = plugin.getPlayerPowersList(targetPlayer).get(plugin.getConfig().getInt("players." + targetPlayer.getName() + ".mode"));
                    } else {
                        player.sendMessage("That player doesn't have a power you can steal!");
                        return;
                    }

                    plugin.getConfig().set("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/froglights", false);
                    plugin.getConfig().set("players." + targetPlayer.getName() + ".powers." + targetPlayerEnabledKey, false);

                    plugin.getConfig().set("players." + plugin.provider.getInfo(player).getName() + ".powers." + targetPlayerEnabledKey, true);

                    player.sendMessage(ChatColor.RED + "You stole " + ChatColor.AQUA + targetPlayerEnabledKey + ChatColor.RED + " from " + ChatColor.AQUA + targetPlayer.getName() + "!");
                    targetPlayer.sendMessage(ChatColor.AQUA + plugin.provider.getInfo(player).getName() + ChatColor.RED + " stole " + ChatColor.AQUA + targetPlayerEnabledKey + "!");

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        plugin.getConfig().set("players." + plugin.provider.getInfo(player).getName() + ".powers." + targetPlayerEnabledKey, false);
                        plugin.getConfig().set("players." + plugin.provider.getInfo(player).getName() + ".powers." + "husbandry/froglights", true);

                        plugin.getConfig().set("players." + targetPlayer.getName() + ".powers." + targetPlayerEnabledKey, true);

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
                    setCooldown(player.getUniqueId(), mimicCooldowns, 120);
                    Player hitPlayer = (Player) result.getHitEntity();

                    plugin.provider.setNamePattern(Pattern.compile("^[a-zA-Z0-9_.§]{1,16}$"));

                    Disguise disguise = Disguise.builder()
                            .setName(ChatColor.RESET + hitPlayer.getName())
                            // you could as well use Disguise.Builder#setSkin(Skin)
                            // or even Disguise.Builder#setSkin(uuid)
                            // it's recommended to run this async since #setSkin from an online API will block the mainthread
                            .setSkin(SkinAPI.MOJANG, hitPlayer.getUniqueId())
                            .build();
                    DisguiseResponse response = plugin.provider.disguise(player, disguise);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            DisguiseManager.getProvider().undisguise(player);
                        }
                    }.runTaskLater(plugin, 20 * 30);
                }
            }
        }
    }
}
