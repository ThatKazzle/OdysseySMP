package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class Beaconator extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    SimpleS5 plugin;

    public Beaconator(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = (Player) plugin.getServer().getPlayer(playerName);
        if (isOnCooldown(player.getUniqueId(), cooldowns)) {

        }
    }
}
