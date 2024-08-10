package PowerClasses;

import kazzleinc.simples5.RandomUtils;
import kazzleinc.simples5.SimpleS5;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class HowDidWeGetHere extends ParentPowerClass implements Listener {

    List<PotionEffectType> potionTypes = Arrays.asList(PotionEffectType.BLINDNESS, PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.WITHER, PotionEffectType.NAUSEA, PotionEffectType.HUNGER, PotionEffectType.DARKNESS, PotionEffectType.MINING_FATIGUE, PotionEffectType.SLOW_FALLING);

    List<PotionEffectType> posPotionTypes = Arrays.asList(PotionEffectType.ABSORPTION, PotionEffectType.REGENERATION, PotionEffectType.SATURATION, PotionEffectType.STRENGTH, PotionEffectType.SPEED, PotionEffectType.RESISTANCE);
    //private static final List<String> AUTOFILL_ARGS_1 = Arrays.asList("withdraw", "powers");

    public final HashMap<UUID, Long> rightClickedCooldowns = new HashMap<>();

    public HowDidWeGetHere(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {

    }

    @EventHandler
    public void onEntityDamageEntityEvent(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player hitPlayer = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            if (this.plugin.getConfig().getBoolean("players." + damager.getName() + ".powers." + "nether/all_effects")) {
                damager.getWorld().spawnParticle(Particle.REVERSE_PORTAL, damager.getLocation().add(new Vector(0, 1, 0)), 30, 5, 5, 5, new Particle.DustOptions(Color.fromRGB(RandomUtils.getRandomIntInRange(0, 255), RandomUtils.getRandomIntInRange(0, 255), RandomUtils.getRandomIntInRange(0, 255)), 2));

                if (RandomUtils.getRandomIntInRange(0, 20) > 19) {
                    PotionEffectType potion = getRandomElement(potionTypes);

                    if (potion == PotionEffectType.HUNGER) {
                        hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 10, 59, false, true));
                    } else {
                        hitPlayer.addPotionEffect(new PotionEffect(potion, 20 * 10, 1, false, true));
                    }


                    hitPlayer.playSound(hitPlayer, Sound.BLOCK_ANVIL_BREAK, 1.f, 1.f);
                    hitPlayer.sendMessage(ChatColor.AQUA + damager.getName() + ChatColor.GREEN + " has given you " + ChatColor.YELLOW + plugin.titleCaseString(potion.getName().replace("_", " ").toLowerCase()) + ChatColor.GREEN + "!");
                    damager.playSound(hitPlayer, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.2f, 1.f);
                    damager.sendMessage(ChatColor.GREEN + "You have inflicted " + ChatColor.AQUA + hitPlayer.getDisplayName() + ChatColor.GREEN + " with " + ChatColor.YELLOW + plugin.titleCaseString(potion.getName().toLowerCase()) + ChatColor.GREEN + "!");
                }

                if (RandomUtils.getRandomIntInRange(0, 30) > 29) {
                    PotionEffectType potion = getRandomElement(posPotionTypes);

                    damager.addPotionEffect(new PotionEffect(potion, 20 * 10, 1, false, true, true));

                    damager.playSound(hitPlayer, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
                    damager.sendMessage(ChatColor.GREEN + "You have been given " + ChatColor.YELLOW + plugin.titleCaseString(potion.getName().replace("_", " ").toLowerCase()) + ChatColor.GREEN + "!");
                }
            }

        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer();
            Player clickedPlayer = (Player) event.getRightClicked();
            if (this.plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + "nether/all_effects") && event.getHand() == EquipmentSlot.HAND && clickedPlayer.getInventory().getItemInMainHand().getType() != Material.AIR) {
                if (isOnCooldown(player.getUniqueId(), rightClickedCooldowns)) {
                    cantUsePowerMessage(player, rightClickedCooldowns, "Item Disable");
                } else if (player.isSneaking() && !isOnCooldown(player.getUniqueId(), rightClickedCooldowns)) {
                    replaceHeldItem(clickedPlayer, clickedPlayer.getInventory().getItemInMainHand(), player);

                    player.sendActionBar(ChatColor.GREEN + "Disabled " + ChatColor.AQUA + clickedPlayer.getName() + ChatColor.GREEN + "!");
                    setCooldown(player.getUniqueId(), rightClickedCooldowns, 30);
                }
            }
        }
    }

    private void replaceHeldItem(Player player, ItemStack stackToReplace, Player otherPlayer) {
        // Get the player's held item
        int origSlot = player.getInventory().getHeldItemSlot();
        ItemStack originalItem = stackToReplace;

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);

        player.sendActionBar(ChatColor.GREEN + "Your item has been " + ChatColor.RED + "disabled" + ChatColor.GREEN + " by " + ChatColor.RED + otherPlayer.getName() + ChatColor.GREEN + "!");

        // Create the barrier item
        ItemStack barrierItem = new ItemStack(Material.STRUCTURE_VOID);

        // Replace the held item with the barrier
        player.getInventory().setItem(origSlot, barrierItem);

        // Schedule a task to restore the original item after 3 seconds (60 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                int i = -1;
                for (ItemStack item : player.getInventory().getContents()) {
                    i++;
                    if (item.getType() == Material.STRUCTURE_VOID) {
                        player.getInventory().setItem(i, originalItem);
                        break;
                    }
                }

                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
                player.sendActionBar(ChatColor.GREEN + "Your item has been " + ChatColor.AQUA + "ENABLED" + ChatColor.GREEN + "!");
            }
        }.runTaskLater(this.plugin, 20 * 5);
    }

    public static PotionEffectType getRandomElement(List<PotionEffectType> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(randomIndex);
    }
}
