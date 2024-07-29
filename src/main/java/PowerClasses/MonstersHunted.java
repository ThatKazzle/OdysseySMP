package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;

import static kazzleinc.simples5.SimpleS5.roundDecimalNumber;

public class MonstersHunted extends ParentPowerClass implements Listener {

    public HashMap<UUID, Long> sphereCooldowns = new HashMap<>();

    private HashMap<Location, BlockState> blockDataHashMap = new HashMap<>();

    private HashMap<UUID, Location> sphereCenterLocations = new HashMap<>();

    private Map<Location, Material> originalBlocks = new HashMap<>();

    private final int sphereRadius = 7;
    private final int cooldownTime = 120;
    private final List<Material> unreplaceableMaterials = Arrays.asList(
            Material.SMOKER, Material.FURNACE, Material.BLAST_FURNACE,
            Material.BEACON, Material.DISPENSER, Material.DROPPER,
            Material.HOPPER, Material.JUKEBOX, Material.COMMAND_BLOCK,
            Material.CHEST, Material.BARREL, Material.ENDER_CHEST,
            Material.RESPAWN_ANCHOR, Material.BREWING_STAND);
    private static final Set<Material> INTERACTABLE_BLOCKS = new HashSet<>(Arrays.asList(
            Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE, Material.BLAST_FURNACE,
            Material.SMOKER, Material.CRAFTING_TABLE, Material.ANVIL, Material.STONE_BUTTON,
            Material.OAK_BUTTON, Material.LEVER, Material.LECTERN, Material.BELL,
            Material.ENCHANTING_TABLE, Material.BREWING_STAND, Material.BEACON,
            Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.NOTE_BLOCK,
            Material.JUKEBOX, Material.COMMAND_BLOCK, Material.STRUCTURE_BLOCK,
            Material.BARREL, Material.GRINDSTONE, Material.LOOM, Material.CARTOGRAPHY_TABLE,
            Material.SMITHING_TABLE, Material.STONECUTTER, Material.LIGHTNING_ROD,
            Material.SCULK_SENSOR, Material.RESPAWN_ANCHOR, Material.SHULKER_BOX
            // Add more interactable blocks as needed
    ));

    public MonstersHunted(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {
        if (Bukkit.getPlayer(playerName).isSneaking()) {
            monstersHuntedAction(playerName);
        } else {
            blackHoleAction(playerName);
        }

    }

    @EventHandler
    public void onPlayerConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        for (Location loc : sphereCenterLocations.values()) {
            if (loc.distance(player.getLocation()) <= sphereRadius) {
                if (event.getItem().getType().equals(Material.CHORUS_FRUIT)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void monstersHuntedAction(String playerName) {
        Player player = this.plugin.getServer().getPlayer(playerName);

        if (this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "adventure/kill_all_mobs")) {
            if (isOnCooldown(player.getUniqueId(), sphereCooldowns)) {
                cantUsePowerMessage(player, sphereCooldowns, "Ice Cage");
            } else if (!isOnCooldown(player.getUniqueId(), sphereCooldowns)) {
                setCooldown(player.getUniqueId(), sphereCooldowns, cooldownTime);

                sphereCenterLocations.put(player.getUniqueId(), player.getLocation());
                saveOriginalBlocks(sphereCenterLocations.get(player.getUniqueId()), sphereRadius);
                //to run the code

                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 8 * 20, 0, false, false, true));

                BukkitTask tickRunner = new BukkitRunnable() {
                    @Override
                    public void run() {
                        createSphere(sphereCenterLocations.get(player.getUniqueId()), sphereRadius, Material.ICE, false);
                    }
                }.runTaskTimer(this.plugin, 0, 1);

                //to exit after 8 seconds
                BukkitTask tickEnder = new BukkitRunnable() {
                    @Override
                    public void run() {
                        tickRunner.cancel();
                        createSphere(sphereCenterLocations.get(player.getUniqueId()), sphereRadius, Material.AIR, true);
                        sphereCenterLocations.remove(player.getUniqueId());
                        this.cancel();
                    }
                }.runTaskLater(this.plugin, 20 * 8);
            }
        }
    }

    private void blackHoleAction(String playerName) {
        Player player = Bukkit.getPlayer(playerName);

        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 10, entity -> entity != player);

        if (result != null) {
            player.sendMessage("Hit location is " + result.getHitPosition());

            player.getWorld().spawnParticle(Particle.FLAME, result.getHitPosition().toLocation(player.getWorld()), 1);
        }

    }

    public void createSphere(Location center, int radius, Material replacementMaterial, boolean isReplacing) {
        // Save original blocks if isReplacing is true
        if (isReplacing) {
            replaceOriginalBlocks();
        } else {
            // Replace blocks to create the sphere
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + y * y + z * z <= radius * radius) {
                            if (x * x + y * y + z * z >= (radius - 1) * (radius - 1)) {
                                Block block = center.getWorld().getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                                if (!unreplaceableMaterials.contains(block.getType())) {
                                    block.setType(replacementMaterial);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void createSphereAnimated(Player player, Location center, int radius, Material material) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            int layers = radius * 2 + 1;
            for (int y = -radius; y <= radius; y++) {
                int finalY = y;
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> createSphereLayer(center, radius, finalY, material), (y + radius) * 2L);
            }
        });
    }

    private void createSphereLayer(Location center, int radius, int yOffset, Material material) {
        saveOriginalBlocks(center, radius);
        double rSquared = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z + yOffset * yOffset <= rSquared) {
                    if (x * x + yOffset * yOffset + z * z >= (radius - 1) * (radius - 1)) {
                        Location blockLocation = center.clone().add(x, yOffset, z);
                        blockLocation.getBlock().setType(material);
                    }
                }
            }
        }
    }

    public Block getBlockBelowPlayer(Player player) {
        // Get the player's location
        Location playerLocation = player.getLocation();

        // Create a new location one block below the player's feet
        Location blockBelowLocation = playerLocation.clone().subtract(0, 1, 0);

        // Get the block at that location
        Block blockBelow = blockBelowLocation.getBlock();

        // While the block is air, continue going down until a non-air block is found
        while (blockBelow.getType() == Material.AIR && blockBelowLocation.getY() > 0) {
            blockBelowLocation.subtract(0, 1, 0);
            blockBelow = blockBelowLocation.getBlock();
        }

        // Return the block below the player
        return blockBelow;
    }

    private void saveOriginalBlocks(Location center, int radius) {
        originalBlocks.clear(); // Clear existing saved blocks

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radius * radius) {
                        if (x * x + y * y + z * z >= (radius - 1) * (radius - 1)) {
                            Block block = center.getWorld().getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);

                            originalBlocks.put(block.getLocation(), block.getType());
                            blockDataHashMap.put(block.getLocation(), block.getState());
                        }
                    }
                }
            }
        }
    }

    private void replaceOriginalBlocks() {
        for (Map.Entry<Location, Material> entry : originalBlocks.entrySet()) {

            Location loc = entry.getKey();
            Material material = entry.getValue();
            Block block = loc.getBlock();
            if (!unreplaceableMaterials.contains(block.getType())) {
                block.setType(material);
            }

        }
        originalBlocks.clear(); // Clear saved blocks after replacing them back
    }


}
