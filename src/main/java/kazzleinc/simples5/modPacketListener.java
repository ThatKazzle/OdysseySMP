package kazzleinc.simples5;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class modPacketListener implements PluginMessageListener {
    public List<String> enabledKeys = new ArrayList<>();

    SimpleS5 plugin;

    public modPacketListener(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    public static final String CHANNEL_NAME = "odysseyclientside:power_channel";
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!enabledKeys.isEmpty()) {
            enabledKeys.clear();
        }
        if (!channel.equals(CHANNEL_NAME)) {
            return;
        }
        // Process the received data
        String data = new String(message);
        //getServer().getLogger().info("Received data from client: " + data);

        // Always act on the player who sent the message. The name in the payload
        // ("power1/<name>") used to be trusted, so any client could act for someone else.
        ArrayList<String> owned = plugin.powerStore.getEnabledPowers(player);
        if (owned != null) enabledKeys.addAll(owned);

        if (!enabledKeys.isEmpty()) {
            String action = data.split("/")[0];

            // The client mod numbers slots 1 and 2 (the /power1 command uses 0 and 1). Kept as-is; see PLAN.md 5.1.
            if (!plugin.powerStore.hasMode(player)) {
                plugin.powerStore.setMode(player, 2);
            }

            if (action.equals("power1")) {
                if (plugin.powerStore.getMode(player) == 1) {
                    plugin.powerStore.setMode(player, 2);
                } else if (plugin.powerStore.getMode(player) == 2) {
                    plugin.powerStore.setMode(player, 1);
                }
            }

            int index = plugin.powerStore.getMode(player) - 1;
            if (index < 0 || index >= enabledKeys.size()) return; // used to throw IndexOutOfBoundsException

            if (action.equals("power1")) {
                player.sendActionBar(ChatColor.GREEN + "Current Power Equipped: " + ChatColor.AQUA + this.plugin.getAdvancementNameFormattedFromUnformattedString(enabledKeys.get(index)));
            } else if (action.equals("power2")) {
                switch (enabledKeys.get(index)) {
                    case "adventure/very_very_frightening":
                        plugin.vvfClass.action(player);
                        break;
                    case "nether/all_effects":
                        plugin.hdwghClass.action(player);
                        break;
                    case "husbandry/complete_catalogue":
                        plugin.catalogueClass.action(player);
                        break;
                    case "adventure/kill_all_mobs":
                        plugin.monstersClass.action(player);
                        break;
                    case "adventure/sniper_duel":
                        plugin.sniperDuelClass.action(player);
                    case "nether/uneasy_alliance":
                        plugin.uneasyAllianceClass.action(player);
                        break;
                }
            }
        }
    }
}