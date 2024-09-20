package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FortniteOdyssey extends ParentPowerClass implements Listener {

    public final HashMap<UUID, Long> fullBoxCooldowns = new HashMap<>();

    public FortniteOdyssey(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        createAndRestoreSquare(player.getLocation(), Material.IRON_BLOCK);
    }

    public class SavedBlock {
        private Location location;
        private BlockData blockData;

        public SavedBlock(Location location, @NotNull BlockData blockData) {
            this.location = location;
            this.blockData = blockData;
        }

        public Location getLocation() {
            return location;
        }

        public BlockData getBlockData() {
            return blockData;
        }
    }

    public void createAndRestoreSquare(Location center, Material newMaterial) {
        // List to store the original blocks
        List<SavedBlock> savedBlocks = new ArrayList<>();

        // Get the coordinates for the 4x4 hollow square
        int startX = center.getBlockX() - 2;  // 2 blocks to the left
        int startZ = center.getBlockZ() - 2;  // 2 blocks back
        int y = center.getBlockY();  // Assume we want the square at the same Y level

        // Loop through and select the border blocks
        for (int x = startX; x < startX + 5; x++) {
            for (int z = startZ; z < startZ + 5; z++) {
                // Skip inner blocks to make it hollow
                if (x != startX && x != startX + 4 && z != startZ && z != startZ + 4) {
                    continue;
                }

                // Get the block at this location
                Location blockLocation = new Location(center.getWorld(), x, y, z);
                Block block = blockLocation.getBlock();

                // Save the original block
                savedBlocks.add(new SavedBlock(blockLocation, block.getBlockData()));

                // Replace the block with the new material (iron block in this case)
                block.setType(newMaterial);
            }
        }

        // Create a delayed task to restore the blocks after 10 seconds (200 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                restoreBlocks(savedBlocks);
            }
        }.runTaskLater(plugin, 200L);  // 200 ticks = 10 seconds
    }

    public void restoreBlocks(List<SavedBlock> savedBlocks) {
        // Loop through the saved blocks and restore them
        for (SavedBlock savedBlock : savedBlocks) {
            Block block = savedBlock.getLocation().getBlock();
            block.setBlockData(savedBlock.getBlockData());  // Restore the original block
        }
    }
}
