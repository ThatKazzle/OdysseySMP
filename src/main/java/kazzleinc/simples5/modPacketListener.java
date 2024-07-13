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

        for (String keys : this.plugin.getConfig().getConfigurationSection("players." + player.getName() + ".powers").getKeys(false)) {
            String key = keys;
            Boolean value = this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + key);

            if (value) {
                enabledKeys.add(key);
            }
        }
        if (!enabledKeys.isEmpty()) {
            String playerName = data.split("/")[1];

            if (this.plugin.getConfig().get("players." + playerName + ".mode") == null) {
                this.plugin.getConfig().set("players." + playerName + ".mode", 2);
            }

            this.plugin.saveConfig();
            if (data.split("/")[0].equals("power1")) {
                if (this.plugin.getConfig().getInt("players." + playerName + ".mode") == 1) {

                    this.plugin.getConfig().set("players." + playerName + ".mode", 2);

                } else if (this.plugin.getConfig().getInt("players." + playerName + ".mode") == 2) {

                    this.plugin.getConfig().set("players." + playerName + ".mode", 1);

                }
                this.plugin.saveConfig();
                plugin.getServer().getPlayer(playerName).sendActionBar(ChatColor.GREEN + "Current Power Equipped: " + ChatColor.AQUA + this.plugin.getAdvancementNameFormattedFromUnformattedString(enabledKeys.get(this.plugin.getConfig().getInt("players." + playerName + ".mode") - 1)));
            } else if (data.split("/")[0].equals("power2")) {
                //this.plugin.getServer().broadcastMessage("player " + playerName + " is in mode " + enabledKeys.get(this.plugin.getConfig().getInt("players." + playerName + ".mode") - 1));
                switch (enabledKeys.get(this.plugin.getConfig().getInt("players." + playerName + ".mode") - 1)) {
                    case "adventure/very_very_frightening":
                        plugin.vvfClass.action(playerName);
                        break;
                    case "nether/all_effects":
                        plugin.hdwghClass.action(playerName);
                        break;
                    case "husbandry/complete_catalogue":
                        plugin.catalogueClass.action(playerName);
                        break;
                    case "adventure/kill_all_mobs":
                        plugin.monstersClass.action(playerName);
                        break;
                    case "adventure/sniper_duel":
                        plugin.sniperDuelClass.action(playerName);
                    case "nether/uneasy_alliance":
                        plugin.uneasyAllianceClass.action(playerName);
                        break;
                }
            }
        }
    }
}