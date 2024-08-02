package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
                    }
                }.runTaskLater(plugin, 20);
            }
        }
    }

    @EventHandler
    public void OnEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            Player damagedPlayer = ((Player) event.getEntity());

            if (fallDamageIgnoreList.contains(damagedPlayer)) {
                event.setCancelled(true);

                fallDamageIgnoreList.remove(damagedPlayer);

                for (Player checkPlayer : Bukkit.getOnlinePlayers()) {
                    if (checkPlayer.getWorld() == damagedPlayer.getWorld()) {
                        if (checkPlayer.getLocation().distance(damagedPlayer.getLocation()) < 7) {
                            Vector dir = checkPlayer.getLocation().toVector().subtract(damagedPlayer.getLocation().toVector()).normalize();

                            dir.setY(0);
                            dir.multiply(0.5);
                            dir.add(new Vector(0, 1, 0));

                            checkPlayer.setVelocity(dir);
                            checkPlayer.setHealth(checkPlayer.getHealth() - 4 < 0 ? 0 : checkPlayer.getHealth() - 4);
                        }
                    }
                }
            }
        }
    }
}
