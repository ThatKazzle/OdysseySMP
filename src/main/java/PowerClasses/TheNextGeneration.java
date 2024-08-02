package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class TheNextGeneration extends ParentPowerClass implements Listener {
    public HashMap<UUID, Long> cooldowns = new HashMap<>();

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

            }
        }
    }
}
