package kazzleinc.simples5;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Key presses from the odysseyclientside mod. Payload is "power1/&lt;name&gt;" (switch power) or
 * "power2/&lt;name&gt;" (use power). The name is ignored: we always act on the player who sent it.
 * Uses the same code as /power1 and /power2, so slots, powers and messages match the commands.
 */
public class modPacketListener implements PluginMessageListener {

    SimpleS5 plugin;

    public modPacketListener(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    public static final String CHANNEL_NAME = "odysseyclientside:power_channel";

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals(CHANNEL_NAME)) return;

        ArrayList<String> owned = plugin.powerStore.getEnabledPowers(player);
        if (owned == null || owned.isEmpty()) return; // no powers: ignore the key press, as before

        String action = new String(message, StandardCharsets.UTF_8).split("/")[0];

        if (action.equals("power1")) {
            String selected = plugin.cyclePowerSlot(player);
            if (selected != null) {
                player.sendActionBar(ChatColor.GREEN + "Current Power Equipped: " + ChatColor.AQUA + plugin.powers.displayName(selected));
            }
        } else if (action.equals("power2")) {
            plugin.usePower(player);
        }
    }
}
