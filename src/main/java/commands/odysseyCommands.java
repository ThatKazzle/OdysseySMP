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
import org.bukkit.event.inventory.InventoryCloseEvent;
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

        AUTOFILL_ARGS_2.addAll(this.plugin.getConfig().getConfigurationSection("defaults.").getKeys(false));
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

                    if (args[1].equals(keys)) {
                        sendDescriptionMessage(player, keys);
                    }
                }

            } else if (args.length == 1 && args[0].equals("powers")) {
                sendPowerStatus(player, plugin.powerStore.getPowerTakenStatus());
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
        if (!(commandSender instanceof Player)) return Collections.emptyList();

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], AUTOFILL_ARGS_1, new ArrayList<>());
        } else if (args.length == 2 && args[0].equals("description")) {
            return StringUtil.copyPartialMatches(args[1], AUTOFILL_ARGS_2, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    public void openGUI(Player player) {
        List<String> ownedPowers = plugin.getPlayerPowersList(player);
        if (ownedPowers == null || ownedPowers.isEmpty()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.f, 1.f);
            player.sendMessage(ChatColor.RED + "You have no powers to withdraw.");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 9, "Choose an Odyssey to withdraw:");

        FileConfiguration config = plugin.getConfig();
        for (String keys : plugin.powerStore.getPowerKeys(player)) {
            String key = keys;
            boolean value = plugin.powerStore.hasPower(player, keys);
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

        if (!event.getView().getTitle().equals("Choose an Odyssey to withdraw:")) return;
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String powerName = clickedItem.getItemMeta().getPersistentDataContainer().get(this.plugin.powerPotionKey, PersistentDataType.STRING);

        if (clickedItem.getItemMeta().getPersistentDataContainer().has(this.plugin.powerPotionKey) && clickedItem.getItemMeta().getPersistentDataContainer().has(this.canClickKey) && event.getRawSlot() <= 1 && !event.isShiftClick()) {
            if (Objects.equals(plugin.wopcClass.playerStoredPower.get(plugin.wopcClass.playerStoleFromPlayer.get(player.getUniqueId())), powerName)) {
                player.sendMessage(ChatColor.RED + "Ye nice try idiot, you cant dupe.");
            } else {
                this.plugin.removePlayerAdvancement(player, plugin.getAdvancementKeyFromFormattedString(powerName));
                plugin.powerStore.setPower(player, plugin.getAdvancementKeyFromFormattedString(powerName), false);

                ItemMeta newMeta = clickedItem.getItemMeta();
                newMeta.getPersistentDataContainer().getKeys().remove(this.canClickKey);
                clickedItem.setItemMeta(newMeta);


                giveItem(player, clickedItem);
                player.closeInventory();

                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.f, 1.f);
                player.sendActionBar(ChatColor.GREEN + "Withdrew " + ChatColor.LIGHT_PURPLE + clickedItem.getItemMeta().getDisplayName() + ChatColor.GREEN + "!");
            }

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
    }

    private void sendAllTakenPowersMessage(Player player) {
        Map<String, Boolean> allPowers = plugin.powerStore.getAllPowers();

        for (Map.Entry<String, Boolean> entry : allPowers.entrySet()) {
            plugin.getLogger().info(entry.getKey() + ": " + entry.getValue());
        }
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
            String powerName = plugin.powers.displayName(entry.getKey());

            player.sendMessage(ChatColor.AQUA + powerName + ": " + status);
        }
    }
}
