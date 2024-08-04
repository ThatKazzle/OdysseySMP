package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TheNextGeneration extends ParentPowerClass implements Listener {
    public HashMap<UUID, Long> cooldowns = new HashMap<>();

    public List<Player> fallDamageIgnoreList = new ArrayList<>();

    public TheNextGeneration(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);

        groundPoundAction(player);
    }

    public void groundPoundAction(Player player) {
        if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "end/dragon_egg")) {
            if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
                setCooldown(player.getUniqueId(), cooldowns, 60 * 2);

                fallDamageIgnoreList.add(player);

                player.setVelocity(new Vector(0, 3, 0));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setVelocity(new Vector(0, -3, 0));

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.getVelocity().getY() >= 0) {
                                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND, 1.f, 1.f);
                                    player.getWorld().playEffect(player.getLocation(), Effect.SMASH_ATTACK, 100);

                                    for (Player checkPlayer : Bukkit.getOnlinePlayers()) {
                                        if (checkPlayer.getWorld() == player.getWorld()) {
                                            if (checkPlayer.getLocation().distance(player.getLocation()) < 7) {
                                                if (checkPlayer != player) {
                                                    Vector dir = checkPlayer.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();

                                                    dir.setY(0);
                                                    dir.multiply(0.5);
                                                    dir.add(new Vector(0, 1, 0));

                                                    checkPlayer.sendMessage(dir.toString());

                                                    checkPlayer.setVelocity(dir);

                                                    if (checkPlayer.getHealth() - 8 <= 0) {
                                                        checkPlayer.setHealth(0);
                                                    } else {
                                                        checkPlayer.setHealth(checkPlayer.getHealth() - 8);
                                                    }

                                                    checkPlayer.damage(0.0001);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }.runTaskTimer(plugin, 0, 1);
                    }
                }.runTaskLater(plugin, 20);
            }
        }
    }

//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void OnEntityDamageEvent(EntityDamageEvent event) {
//        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
//            Player damagedPlayer = ((Player) event.getEntity());
//
//            if (fallDamageIgnoreList.contains(damagedPlayer) && plugin.getConfig().getBoolean("players." + damagedPlayer.getName() + ".powers." + "end/dragon_egg")) {
//                event.setCancelled(true);
//
//                fallDamageIgnoreList.remove(damagedPlayer);
//
//                damagedPlayer.getWorld().playSound(damagedPlayer.getLocation(), Sound.ITEM_MACE_SMASH_GROUND, 1.f, 1.f);
//                damagedPlayer.getWorld().playEffect(damagedPlayer.getLocation(), Effect.SMASH_ATTACK, 100);
//
//                for (Player checkPlayer : Bukkit.getOnlinePlayers()) {
//                    if (checkPlayer.getWorld() == damagedPlayer.getWorld()) {
//                        if (checkPlayer.getLocation().distance(damagedPlayer.getLocation()) < 7) {
//                            if (checkPlayer != damagedPlayer) {
//                                Vector dir = checkPlayer.getLocation().toVector().subtract(damagedPlayer.getLocation().toVector()).normalize();
//
//                                dir.setY(0);
//                                dir.multiply(0.5);
//                                dir.add(new Vector(0, 1, 0));
//
//                                checkPlayer.sendMessage(dir.toString());
//
//                                checkPlayer.setVelocity(dir);
//                                checkPlayer.damage(0.0001);
//
//                                if (checkPlayer.getHealth() - 8 <= 0) {
//                                    checkPlayer.setHealth(0);
//                                } else {
//                                    checkPlayer.setHealth(checkPlayer.getHealth() - 8);
//                                }
//
//
//                            }
//                        }
//                    }
//                }
//
//                for (Entity thing : damagedPlayer.getNearbyEntities(7, 2, 7)) {
//                    if (!(thing instanceof Player)) {
//
//
//                        Vector dir = thing.getLocation().toVector().subtract(damagedPlayer.getLocation().toVector()).normalize();
//
//                        dir.setY(0);
//                        dir.multiply(0.5);
//                        dir.add(new Vector(0, 1, 0));
//
//                        thing.sendMessage(dir.toString());
//
//                        thing.setVelocity(dir);
//                    }
//                }
//
//                damagedPlayer.setVelocity(new Vector(0, 0.5, 0));
//            }
//        }
//    }
}
