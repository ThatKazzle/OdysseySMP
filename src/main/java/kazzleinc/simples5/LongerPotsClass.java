package kazzleinc.simples5;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BrewingStartEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class LongerPotsClass {
    SimpleS5 plugin;

    NamespacedKey strengthCraft;

    public LongerPotsClass(SimpleS5 plugin) {
        this.plugin = plugin;
        strengthCraft = new NamespacedKey(plugin, "strength_special_recipe");

        setupRecipe();
    }

    public void setupRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(strengthCraft, getStrengthTwoLongPotion());

        ItemStack strengthPotion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta strengthMeta = (PotionMeta) strengthPotion.getItemMeta();
        strengthMeta.setBasePotionData(new PotionData(PotionType.STRENGTH, false, true)); // Strength II, normal duration (1:30)
        strengthPotion.setItemMeta(strengthMeta);

        recipe.addIngredient(strengthPotion);
        recipe.addIngredient(Material.REDSTONE_BLOCK);

        plugin.getServer().addRecipe(recipe);
    }

    public ItemStack getStrengthTwoLongPotion() {
        ItemStack origPotion = new ItemStack(Material.SPLASH_POTION);

        PotionMeta meta = (PotionMeta) origPotion.getItemMeta();

        meta.setDisplayName(ChatColor.RESET + "Splash Potion of Strength (8:00)");
        meta.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 8 * 60 * 20, 1), true);

        origPotion.setItemMeta(meta);
        return origPotion;
    }
}
