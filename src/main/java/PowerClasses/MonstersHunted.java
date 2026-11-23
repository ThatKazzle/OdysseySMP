package PowerClasses;

import kazzleinc.simples5.ParticleUtils;
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
    public HashMap<UUID, Long> blackHoleCooldowns = new HashMap<>();

    private HashMap<UUID, Location> sphereCenterLocations = new HashMap<>();

    /**
     * The blocks each active ice sphere replaced, by caster, so each sphere puts back its own area.
     * (One shared map meant a second sphere wiped the first one's copy and left ice in the world.)
     */
    private final Map<UUID, Map<Location, Material>> originalBlocks = new HashMap<>();

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
    public void action(Player player) {
        if (!player.isSneaking()) {
            monstersHuntedAction(player);
        } else {
            blackHoleAction(player);
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

    @Override
    public String getCooldownString(Player player, HashMap<UUID, Long> cooldownMap, String powerName) {
        return "" + ChatColor.AQUA + powerName + getCooldownTimeLeft(player.getUniqueId(), cooldownMap) + ChatColor.BOLD + ChatColor.GOLD + " | " + ChatColor.RESET + ChatColor.AQUA + "Black Hole: " + getCooldownTimeLeft(player.getUniqueId(), blackHoleCooldowns);
    }

    private void monstersHuntedAction(Player player) {

        if (plugin.powerStore.hasPower(player, "adventure/kill_all_mobs")) {
            if (isOnCooldown(player.getUniqueId(), sphereCooldowns)) {
                cantUsePowerMessage(player, sphereCooldowns, "Ice Cage");
            } else if (!isOnCooldown(player.getUniqueId(), sphereCooldowns)) {
                setCooldown(player.getUniqueId(), sphereCooldowns, cooldownTime);

                UUID casterId = player.getUniqueId();
                Location center = player.getLocation();
                sphereCenterLocations.put(casterId, center);
                saveOriginalBlocks(casterId, center, sphereRadius);

                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 8 * 20, 0, false, false, true));

                BukkitTask tickRunner = new BukkitRunnable() {
                    @Override
                    public void run() {
                        createSphere(center, sphereRadius, Material.ICE);
                    }
                }.runTaskTimer(this.plugin, 0, 1);

                //to exit after 8 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        tickRunner.cancel();
                        restoreOriginalBlocks(casterId);
                        sphereCenterLocations.remove(casterId);
                    }
                }.runTaskLater(this.plugin, 20 * 8);
            }
        }
    }

    private void blackHoleAction(Player player) {

        if (plugin.powerStore.hasPower(player, "adventure/kill_all_mobs")) {
            if (!isOnCooldown(player.getUniqueId(), blackHoleCooldowns)) {
                RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 10, FluidCollisionMode.NEVER, true);

                if (result != null) {
                    setCooldown(player.getUniqueId(), blackHoleCooldowns, 60 * 3);
                    player.getWorld().spawnParticle(Particle.DUST, result.getHitPosition().toLocation(player.getWorld()), 1, new Particle.DustOptions(Color.RED, 1));

                    BukkitTask particleRunner = new BukkitRunnable() {
                        @Override
                        public void run() {
                            double damageToDo;
                            double mappedDamage;

                            ParticleUtils.createParticleSphere(result.getHitPosition().toLocation(player.getWorld()), 2, 15, Particle.DUST, Color.BLACK, 2);
                            ParticleUtils.createParticleRing(result.getHitPosition().toLocation(player.getWorld()), 4, 20, Particle.DUST, Color.fromRGB(255, 179, 0), 2);
                            for (Player playerCheck : SimpleS5.getPlayersInRange(result.getHitPosition().toLocation(player.getWorld()), 10)) {
                                if (player != playerCheck && !playerCheck.isDead()) {
                                    damageToDo = playerCheck.getLocation().distance(result.getHitPosition().toLocation(playerCheck.getWorld()));
                                    mappedDamage = plugin.mapValue(damageToDo, 0, 10, 0.67, 0.1);

                                    if (playerCheck.getHealth() - mappedDamage < 0) {
                                        if (playerCheck.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) {
                                            playerCheck.damage(20);
                                        } else {
                                            playerCheck.setHealth(0);
                                        }

                                    } else {
                                        playerCheck.setHealth(playerCheck.getHealth() - mappedDamage);
                                    }

                                    playerCheck.setSaturation(0);
                                    playerCheck.damage(0.01);
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 0, 5);

                    BukkitTask puller = new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (Player playerCheck : SimpleS5.getPlayersInRange(result.getHitPosition().toLocation(player.getWorld()), 10)) {
                                Vector direction = result.getHitPosition().subtract(playerCheck.getLocation().toVector()).normalize();

                                if (playerCheck != player) {
                                    playerCheck.setVelocity(playerCheck.getVelocity().add(direction.multiply(0.05)));
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 0, 0);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            particleRunner.cancel();
                            puller.cancel();
                        }
                    }.runTaskLater(plugin, 20 * 5);
                } else {
                    player.sendMessage(ChatColor.RED + "Nothing to spawn on.");
                }
            } else {
                cantUsePowerMessage(player, blackHoleCooldowns, "Black Hole");
            }
        }
    }

    /** Sets every block in the sphere's shell to the material, except blocks that must not be replaced. */
    public void createSphere(Location center, int radius, Material replacementMaterial) {
        for (Block block : shellBlocks(center, radius)) {
            if (!unreplaceableMaterials.contains(block.getType())) {
                block.setType(replacementMaterial);
            }
        }
    }

    /** Puts back every ice sphere still standing. Called when the plugin is disabled. */
    @Override
    public void onShutdown() {
        for (UUID casterId : new ArrayList<>(originalBlocks.keySet())) {
            restoreOriginalBlocks(casterId);
        }
        sphereCenterLocations.clear();
    }

    private List<Block> shellBlocks(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distanceSquared = x * x + y * y + z * z;
                    if (distanceSquared <= radius * radius && distanceSquared >= (radius - 1) * (radius - 1)) {
                        blocks.add(center.getWorld().getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z));
                    }
                }
            }
        }
        return blocks;
    }

    private void saveOriginalBlocks(UUID casterId, Location center, int radius) {
        Map<Location, Material> saved = new HashMap<>();
        for (Block block : shellBlocks(center, radius)) {
            Location loc = block.getLocation();
            // If another sphere already turned this block to ice, its copy holds the real block.
            Material original = block.getType();
            for (Map<Location, Material> other : originalBlocks.values()) {
                if (other.containsKey(loc)) {
                    original = other.get(loc);
                    break;
                }
            }
            saved.put(loc, original);
        }
        originalBlocks.put(casterId, saved);
    }

    private void restoreOriginalBlocks(UUID casterId) {
        Map<Location, Material> saved = originalBlocks.remove(casterId);
        if (saved == null) return;

        for (Map.Entry<Location, Material> entry : saved.entrySet()) {
            Location loc = entry.getKey();
            // Still part of another active sphere: that sphere puts it back when it ends.
            boolean stillInUse = originalBlocks.values().stream().anyMatch(other -> other.containsKey(loc));
            if (stillInUse) continue;

            Block block = loc.getBlock();
            if (!unreplaceableMaterials.contains(block.getType())) {
                block.setType(entry.getValue());
            }
        }
    }
}
