package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Bullseye extends ParentPowerClass implements Listener {

    public final HashMap<UUID, Long> sonicBoomCooldowns = new HashMap<>();
    public final HashMap<UUID, Long> wardenedHelpCooldowns = new HashMap<>();

    public Bullseye(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (!player.isSneaking()) {
            sonicBoomAction(player);
        } else {
            wardenHiredHelpAction(player);
        }

    }

    public void sonicBoomAction(Player player) {
        if (!isOnCooldown(player.getUniqueId(), sonicBoomCooldowns)) {
            
        }
    }

    public void wardenHiredHelpAction(Player player) {

    }
}
