package kazzleinc.simples5;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PowerPotionItem {

    private ItemStack itemStack;
    private SimpleS5 plugin;

    private NamespacedKey powerKey = null;

    public PowerPotionItem(SimpleS5 plugin, String power, NamespacedKey powerKey) {
        this.itemStack = new ItemStack(Material.AMETHYST_SHARD);
        this.plugin = plugin;

        this.powerKey = powerKey;
        setPower(power);
        setDisplayName(power);
    }

    public void setPower(String power) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(powerKey, PersistentDataType.STRING, power);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            itemStack.setItemMeta(meta);
        }
    }

    public String getPower() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(powerKey, PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(powerKey, PersistentDataType.STRING);
        }
        return null;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setDisplayName(String displayName) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            //this.plugin.getServer().broadcastMessage(meta.getPersistentDataContainer().get(powerKey, PersistentDataType.STRING));
            List<String> loreConf;
            String nameToAdd = "";

            switch (meta.getPersistentDataContainer().get(powerKey, PersistentDataType.STRING)) {
                case "All Effects":
                    nameToAdd = "How Did We Get Here?";
                    loreConf = this.plugin.getConfig().getStringList("descriptions." + "all_effects");
                    break;
                case "Kill All Mobs":
                    nameToAdd = "Monsters Hunted";
                    loreConf = this.plugin.getConfig().getStringList("descriptions." + "kill_all_mobs");
                    break;
                case "Froglights":
                    nameToAdd = "With Our Powers Combined";
                    loreConf = this.plugin.getConfig().getStringList("descriptions." + "froglights");
                    break;
                case "Ride Strider In Overworld Lava":
                    nameToAdd = "Feels Like Home";
                    loreConf = this.plugin.getConfig().getStringList("descriptions." + "ride_strider_in_overworld_lava");
                    break;
                case "Dragon Egg":
                    nameToAdd = "The Next Generation";
                    loreConf = this.plugin.getConfig().getStringList("descriptions." + "dragon_egg");
                    break;
                case "Create Full Beacon":
                    nameToAdd = "Beaconator";
                    loreConf = this.plugin.getConfig().getStringList("descriptions." + "create_full_beacon");
                    break;
                default:
                    nameToAdd = displayName;
                    loreConf = this.plugin.getConfig().getStringList("descriptions." + this.plugin.getAdvancementNameUnformattedFromFormattedString(meta.getPersistentDataContainer().get(powerKey, PersistentDataType.STRING)));
                    break;
            }

            meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.LIGHT_PURPLE + nameToAdd);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "This Odyssey has: ");

            for (String line : loreConf) {
                lore.add("§r§b" + line);
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
    }
}
