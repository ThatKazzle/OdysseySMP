package commands;

import kazzleinc.simples5.PowerPotionItem;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class odysseyCommands implements CommandExecutor, TabCompleter, Listener {
    SimpleS5 plugin;
    String usageString = ChatColor.RED + "usage: /odyssey <withdraw|powers>";

    private static final List<String> AUTOFILL_ARGS_1 = Arrays.asList("withdraw", "description", "powers");
    private static final List<String> AUTOFILL_ARGS_2 = new ArrayList<>();

    private NamespacedKey canClickKey;


    public odysseyCommands(SimpleS5 plugin) {
        this.plugin = plugin;

        canClickKey = new NamespacedKey(plugin, "isInGUI");
    }



    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1 && args[0].equals("withdraw")) {
                openGUI(player);

            } else if (args.length == 2 && args[0].equals("description")) {
                for (String keys : this.plugin.getConfig().getConfigurationSection("defaults.").getKeys(false)) {
                    String value = this.plugin.getConfig().getString("defaults." + keys);
                    AUTOFILL_ARGS_2.add(keys);

                    if (args[1].equals(keys)) {
                        sendDescriptionMessage(player, keys);
                    } else {
                        player.sendMessage(ChatColor.RED + "Usage: /odyssey description [key of power you want description of]");
                    }
                }

            } else if (args.length == 1 && args[0].equals("powers")) {
                sendPowerStatus(player, checkPowerStatus());
            } else {
                player.sendMessage(ChatColor.RED + "Usage: " + command.getUsage());
            }

            return true;
        } else {
            return false;
        }

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) commandSender;
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], AUTOFILL_ARGS_1, new ArrayList<>());
        } else if (args.length == 2 && args[0].equals("powers")) {
            for (String keys : this.plugin.getConfig().getConfigurationSection("defaults.").getKeys(false)) {
                String value = this.plugin.getConfig().getString("defaults." + keys);
                AUTOFILL_ARGS_2.add(keys);
            }
            return StringUtil.copyPartialMatches(args[1], AUTOFILL_ARGS_2, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "Choose an effect to withdraw:");

        FileConfiguration config = plugin.getConfig();
        for (String keys : this.plugin.getConfig().getConfigurationSection("players." + player.getName() + ".powers").getKeys(false)) {
            String key = keys;
            Boolean value = this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + keys);
            if (value) {
                ItemStack item = new PowerPotionItem(this.plugin, this.plugin.getAdvancementNameFormattedFromUnformattedString(keys), this.plugin.powerPotionKey).getItemStack();

                ItemMeta meta = item.getItemMeta();

                meta.getPersistentDataContainer().set(canClickKey, PersistentDataType.BOOLEAN, true);

                item.setItemMeta(meta);

                gui.addItem(item);
            }

        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals("Choose an effect to withdraw:")) return;
        if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String powerName = clickedItem.getItemMeta().getPersistentDataContainer().get(this.plugin.powerPotionKey, PersistentDataType.STRING);

        if (clickedItem.getItemMeta().getPersistentDataContainer().has(this.plugin.powerPotionKey) && clickedItem.getItemMeta().getPersistentDataContainer().has(this.canClickKey) && event.getSlot() <= 1) {
            this.plugin.removePlayerAdvancement(player, plugin.getAdvancementKeyFromFormattedString(powerName));
            this.plugin.getConfig().set("players." + player.getName() + ".powers." +  plugin.getAdvancementKeyFromFormattedString(powerName), false);

            this.plugin.saveConfig();

            giveItem(player, clickedItem);
            player.closeInventory();

            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.f, 1.f);
            player.sendActionBar(ChatColor.GREEN + "Withdrew " + ChatColor.LIGHT_PURPLE + clickedItem.getItemMeta().getDisplayName() + ChatColor.GREEN + "!");
        }

    }

    public void giveItem(Player player, ItemStack item) {
        if (player.getInventory().addItem(item).isEmpty()) {

        } else {
            Location loc = player.getLocation();
            player.getWorld().dropItemNaturally(loc, item);
        }
    }

    private void sendDescriptionMessage(Player player, String key) {
        player.sendMessage(ChatColor.AQUA + "Description for " + ChatColor.GOLD + ChatColor.BOLD + key + ChatColor.RESET + ChatColor.AQUA + ":");
        for (String curMessage : plugin.getConfig().getStringList("descriptions." + key)) {
            player.sendMessage(curMessage);
        }
        AUTOFILL_ARGS_2.clear();
    }

    private void sendAllTakenPowersMessage(Player player) {
        Map<String, Boolean> allPowers = plugin.getAllPowers(plugin.getConfig());

        for (Map.Entry<String, Boolean> entry : allPowers.entrySet()) {
            plugin.getLogger().info(entry.getKey() + ": " + entry.getValue());
        }
    }

    private Map<String, Boolean> checkPowerStatus() {
        Map<String, Boolean> powerStatus = new HashMap<>();

        // Get the defaults section
        ConfigurationSection defaultsSection = plugin.getConfig().getConfigurationSection("defaults");
        if (defaultsSection == null) {
            return powerStatus; // Return empty map if "defaults" section is not found
        }

        // Get all default power keys
        Set<String> defaultPowerKeys = defaultsSection.getKeys(false);

        // Initialize all powers as "not taken"
        for (String defaultPowerKey : defaultPowerKeys) {
            powerStatus.put(defaultPowerKey, false);
        }

        // Get the "players" section
        ConfigurationSection playersSection = plugin.getConfig().getConfigurationSection("players");
        if (playersSection == null) {
            return powerStatus; // Return if "players" section is not found
        }

        // Loop through each player
        Set<String> playerKeys = playersSection.getKeys(false);
        for (String playerKey : playerKeys) {
            ConfigurationSection powersSection = playersSection.getConfigurationSection(playerKey + ".powers");
            if (powersSection != null) {
                // Loop through each power for the player
                for (String defaultPowerKey : defaultPowerKeys) {
                    String powerPath = defaultsSection.getString(defaultPowerKey);
                    if (powersSection.getBoolean(powerPath, false)) {
                        powerStatus.put(defaultPowerKey, true); // Mark as "taken" if any player has it set to true
                    }
                }
            }
        }

        return powerStatus;
    }

//    private void sendPowerStatus(Player player, Map<String, Boolean> powerStatus) {
//        int maxLength = 0;
//
//        // Find the longest key length
//        for (String key : powerStatus.keySet()) {
//            if (key.length() > maxLength) {
//                maxLength = key.length();
//            }
//        }
//
//        // Send the formatted power status to the player
//        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "List of powers that are taken: ");
//        player.sendMessage("");
//
//        for (Map.Entry<String, Boolean> entry : powerStatus.entrySet()) {
//            String paddedKey = String.format("%-" + (maxLength - entry.getKey().length()) + "s", entry.getKey());
//
//            String status = entry.getValue() ? ChatColor.RED + "Taken" : ChatColor.GREEN + "Not Taken";
//            player.sendMessage(ChatColor.AQUA + paddedKey + ": " + status);
//        }
//    }

    private void sendPowerStatus(Player player, Map<String, Boolean> powerStatus) {
        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "List of powers that are currently taken: ");

        for (Map.Entry<String, Boolean> entry : powerStatus.entrySet()) {
            String status = entry.getValue() ? ChatColor.RED + "Taken" : ChatColor.GREEN + "Not Taken";
            player.sendMessage(ChatColor.AQUA + entry.getKey() + ": " + status);
        }
    }
}
