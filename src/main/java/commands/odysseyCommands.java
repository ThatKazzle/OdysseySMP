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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class odysseyCommands implements CommandExecutor, TabCompleter, Listener {
    SimpleS5 plugin;
    String usageString = ChatColor.RED + "usage: /odyssey <withdraw|powers>";

    private static final List<String> AUTOFILL_ARGS_1 = Arrays.asList("withdraw", "powers");
    private static final List<String> AUTOFILL_ARGS_2 = new ArrayList<>();

    public odysseyCommands(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1 && args[0].equals("withdraw")) {
                openGUI(player);

            } else if (args.length == 2 && args[0].equals("powers")) {
                for (String keys : this.plugin.getConfig().getConfigurationSection("defaults.").getKeys(false)) {
                    String value = this.plugin.getConfig().getString("defaults." + keys);
                    AUTOFILL_ARGS_2.add(keys);

                    if (args[1].equals(keys)) {
                        sendDescriptionMessage(player, keys);
                    }
                }

            } else {
                player.sendMessage(usageString);
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
                gui.addItem(item);
            }

        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals("Choose an effect to withdraw:")) return;
        if (!event.getAction().equals(InventoryAction.PICKUP_ONE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String powerName = clickedItem.getItemMeta().getPersistentDataContainer().get(this.plugin.powerPotionKey, PersistentDataType.STRING);

        if (clickedItem.getItemMeta().getPersistentDataContainer().has(this.plugin.powerPotionKey) && event.getSlot() <= 1) {
            this.plugin.removePlayerAdvancement(player, plugin.getAdvancementKeyFromFormattedString(powerName));
            this.plugin.getConfig().set("players." + player.getName() + ".powers." +  plugin.getAdvancementKeyFromFormattedString(powerName), false);

            if (clickedItem.getItemMeta().getDisplayName().equals("Complete Catalogue")) {
                this.plugin.catalogueClass.removeCataloguePower(player);
            }

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
        player.sendMessage(plugin.getConfig().getStringList("defaults.") + key);
        AUTOFILL_ARGS_2.clear();
    }
}
