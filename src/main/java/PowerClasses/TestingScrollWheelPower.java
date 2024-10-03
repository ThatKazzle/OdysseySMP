package PowerClasses;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class TestingScrollWheelPower extends ParentPowerClass implements Listener {
    public final HashMap<UUID, Integer> powerLevel = new HashMap<>();
    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public TestingScrollWheelPower(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (hasPower(player, "adventure/trim_with_all_exclusive_armor_patterns")) {
            if (powerLevel.containsKey(player.getUniqueId())) {
                player.sendMessage("You are on power " + powerLevel.get(player.getUniqueId()));
            } else {
                powerLevel.put(player.getUniqueId(), 1);
                player.sendMessage("You are on power " + powerLevel.get(player.getUniqueId()));
            }
        }

    }

    @EventHandler
    public void onPlayerChangeSlotEvent(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && hasPower(player, "adventure/trim_with_all_exclusive_armor_patterns")) {
            if (!powerLevel.containsKey(player.getUniqueId())) powerLevel.put(player.getUniqueId(), 1);

            if (event.getNewSlot() - event.getPreviousSlot() == 1) {
                if (powerLevel.get(player.getUniqueId()) + 1 > 7) {
                    powerLevel.put(player.getUniqueId(), 1);
                } else {
                    powerLevel.put(player.getUniqueId(), (powerLevel.get(player.getUniqueId()) + 1));
                }
            }

            if (event.getNewSlot() - event.getPreviousSlot() == -1) {
                if (powerLevel.get(player.getUniqueId()) - 1 < 1) {
                    powerLevel.put(player.getUniqueId(), 7);
                } else {
                    powerLevel.put(player.getUniqueId(), (powerLevel.get(player.getUniqueId()) - 1));
                }
            }
        }
    }
}
