package PowerClasses;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FortniteOdyssey extends ParentPowerClass implements Listener {

    public final HashMap<UUID, Long> fullBoxCooldowns = new HashMap<>();
    public final HashMap<UUID, Long> heavySniperCooldowns = new HashMap<>();

    private final HashSet<UUID> shootsPowerBow = new HashSet<>();

    public final HashSet<Location> protectedBlocks = new HashSet<>();

    List<SavedBlock> savedBlocks = new ArrayList<>();

    NamespacedKey dataKey;

    private final List<Material> unreplaceableMaterials = Arrays.asList(
            Material.SMOKER, Material.FURNACE, Material.BLAST_FURNACE,
            Material.BEACON, Material.DISPENSER, Material.DROPPER,
            Material.HOPPER, Material.JUKEBOX, Material.COMMAND_BLOCK,
            Material.CHEST, Material.BARREL, Material.ENDER_CHEST,
            Material.RESPAWN_ANCHOR, Material.BREWING_STAND);

    public FortniteOdyssey(SimpleS5 plugin) {
        super(plugin);

        dataKey = new NamespacedKey(plugin, "triple_damage_arrow");
    }

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        if (shootsPowerBow.contains(player.getUniqueId())) {
            return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Heavy Sniper: " + ChatColor.BLUE + ChatColor.BOLD + "Activated";
        } else {
            return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Heavy Sniper: " + getCooldownTimeLeft(player.getUniqueId(), heavySniperCooldowns);
        }
    }

    @Override
    public void action(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (!player.isSneaking()) {
            createAndRestoreCube(player, Material.IRON_BLOCK);
        } else {
            heavySniperAbility(player);
        }
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

    public void createAndRestoreCube(Player player, Material newMaterial) {
        if (!isOnCooldown(player.getUniqueId(), fullBoxCooldowns)) {
            setCooldown(player.getUniqueId(), fullBoxCooldowns, 120);
            Location center = player.getLocation();
            // Get the coordinates for the 7x7x7 hollow cube
            int startX = center.getBlockX() - 3;  // 3 blocks to the left
            int startY = center.getBlockY() - 3;  // 3 blocks down
            int startZ = center.getBlockZ() - 3;  // 3 blocks back

            // Loop through the cube area
            for (int x = startX; x < startX + 7; x++) {
                for (int y = startY; y < startY + 7; y++) {
                    for (int z = startZ; z < startZ + 7; z++) {
                        // Hollow out the center 5x5x5 space
                        if (x > startX && x < startX + 6 && y > startY && y < startY + 6 && z > startZ && z < startZ + 6) {
                            continue;  // Skip inner blocks (hollow part)
                        }

                        // Get the block at this location
                        Location blockLocation = new Location(center.getWorld(), x, y, z);
                        Block block = blockLocation.getBlock();
                        protectedBlocks.add(blockLocation);

                        // Save the original block
                        if (!unreplaceableMaterials.contains(block.getType())) {
                            savedBlocks.add(new SavedBlock(blockLocation, block.getBlockData()));

                            // Replace the block with the new material (iron block in this case)
                            block.setType(newMaterial);
                        }
                    }
                }
            }

            BukkitTask replacer = new BukkitRunnable() {
                @Override
                public void run() {
                    for (int x = startX; x < startX + 7; x++) {
                        for (int y = startY; y < startY + 7; y++) {
                            for (int z = startZ; z < startZ + 7; z++) {
                                // Hollow out the center 5x5x5 space
                                if (x > startX && x < startX + 6 && y > startY && y < startY + 6 && z > startZ && z < startZ + 6) {
                                    continue;  // Skip inner blocks (hollow part)
                                }

                                // Get the block at this location
                                Location blockLocation = new Location(center.getWorld(), x, y, z);
                                Block block = blockLocation.getBlock();

                                // Save the original block
                                if (!unreplaceableMaterials.contains(block.getType())) {

                                    // Replace the block with the new material (iron block in this case)
                                    block.setType(newMaterial);
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(plugin, 0, 2);

            // Create a delayed task to restore the blocks after 10 seconds (200 ticks)
            new BukkitRunnable() {
                @Override
                public void run() {
                    replacer.cancel();
                    restoreBlocks();
                }
            }.runTaskLater(plugin, 200L);  // 200 ticks = 10 seconds
        }
    }

    public void heavySniperAbility(Player player) {
        if (shootsPowerBow.contains(player.getUniqueId())) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.f);
            player.sendMessage(ChatColor.RED + "Already activated.");
        } else {
            if (!isOnCooldown(player.getUniqueId(), heavySniperCooldowns)) {
                player.sendMessage(ChatColor.GOLD + "Your next bow shot does 3x damage.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.f);

                shootsPowerBow.add(player.getUniqueId());
            }
        }
    }

    public void restoreBlocks() {
        // Loop through the saved blocks and restore them
        for (SavedBlock savedBlock : savedBlocks) {
            Block block = savedBlock.getLocation().getBlock();
            block.setBlockData(savedBlock.getBlockData());  // Restore the original block
        }
        savedBlocks.clear();
    }

    @EventHandler
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (shootsPowerBow.contains(player.getUniqueId())) {
                Arrow arrow = (Arrow) event.getProjectile();
                //arrow.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(8));

                arrow.setDamage(arrow.getDamage() * 4);

                shootsPowerBow.remove(player.getUniqueId());
                setCooldown(player.getUniqueId(), heavySniperCooldowns, 60);
            }
        }
    }

    @EventHandler
    public void onHitArrowEvent(ProjectileHitEvent event) {

    }

    @EventHandler
    public void onBreakBlockEvent(BlockBreakEvent event) {
        if (protectedBlocks.contains(event.getBlock().getLocation())) {
            event.setDropItems(false);
        }
    }
}
