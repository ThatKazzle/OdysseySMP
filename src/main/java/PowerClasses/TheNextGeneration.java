package PowerClasses;

import kazzleinc.simples5.SimpleS5;
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

    }


}
