package kazzleinc.simples5;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;

import java.util.HashMap;
import java.util.Map;

public class TrimUtils {

    private final Map<Player, Map<Integer, ItemStack>> previousArmorTrims = new HashMap<>();

    public void copyArmorTrims(Player targetPlayer, Player originalPlayer) {
        // Save the current state of the original player's armor
        saveCurrentArmorState(originalPlayer);

        ItemStack[] targetArmor = targetPlayer.getInventory().getArmorContents();
        ItemStack[] originalArmor = originalPlayer.getInventory().getArmorContents();

        for (int i = 0; i < targetArmor.length; i++) {
            ItemStack targetItem = targetArmor[i];
            ItemStack originalItem = originalArmor[i];

            if (targetItem != null && originalItem != null && targetItem.getType() == originalItem.getType()) {
                ArmorMeta targetMeta = (ArmorMeta) targetItem.getItemMeta();
                ArmorMeta originalMeta = (ArmorMeta) originalItem.getItemMeta();

                if (targetMeta.hasTrim()) {
                    originalMeta.setTrim(targetMeta.getTrim());
                } else {
                    originalMeta.setTrim(null);
                }
                originalItem.setItemMeta(originalMeta);
            }
        }

        originalPlayer.getInventory().setArmorContents(originalArmor);
    }

    public void undoCopyArmorTrims(Player originalPlayer) {
        if (previousArmorTrims.containsKey(originalPlayer)) {
            Map<Integer, ItemStack> savedArmor = previousArmorTrims.get(originalPlayer);

            ItemStack[] currentArmor = originalPlayer.getInventory().getArmorContents();
            for (int i = 0; i < currentArmor.length; i++) {
                if (savedArmor.containsKey(i)) {
                    currentArmor[i] = savedArmor.get(i);
                }
            }

            originalPlayer.getInventory().setArmorContents(currentArmor);
            previousArmorTrims.remove(originalPlayer);
        }
    }

    private void saveCurrentArmorState(Player originalPlayer) {
        ItemStack[] originalArmor = originalPlayer.getInventory().getArmorContents();
        Map<Integer, ItemStack> savedArmor = new HashMap<>();

        for (int i = 0; i < originalArmor.length; i++) {
            if (originalArmor[i] != null && originalArmor[i].getType() != Material.AIR) {
                savedArmor.put(i, originalArmor[i].clone());
            }
        }

        previousArmorTrims.put(originalPlayer, savedArmor);
    }
}
