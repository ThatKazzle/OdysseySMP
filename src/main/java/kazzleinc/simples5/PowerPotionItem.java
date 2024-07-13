package kazzleinc.simples5;

import com.sun.tools.javac.jvm.Items;
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
            switch (meta.getPersistentDataContainer().get(powerKey, PersistentDataType.STRING)) {
                case "All Effects":
                    meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.LIGHT_PURPLE + "How Did We Get Here?");
                    loreConf = this.plugin.getConfig().getStringList("descriptions." + "all_effects");
                    break;
                case "Kill All Mobs":
                    meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.LIGHT_PURPLE + "Monsters Hunted");
                    loreConf = this.plugin.getConfig().getStringList("descriptions." + "kill_all_mobs");
                    break;
                default:
                    meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.LIGHT_PURPLE + displayName);
                    loreConf = this.plugin.getConfig().getStringList("descriptions." + this.plugin.getAdvancementNameUnformattedFromFormattedString(ChatColor.stripColor(meta.getDisplayName())));
                    break;
            }

            List<String> lore = new ArrayList<>();

            for (String line : loreConf) {
                lore.add("§r§b" + line);
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
    }
}
