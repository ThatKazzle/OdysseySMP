package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static kazzleinc.simples5.SimpleS5.roundDecimalNumber;

public class SniperDuel extends ParentPowerClass implements Listener {
    SimpleS5 plugin;

    public final HashMap<UUID, Long> cooldowns = new HashMap<>();

    private final List<Player> affectedPlayers = new ArrayList<>();

    public SniperDuel(SimpleS5 plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public void action(String playerName) {
        Player player = this.plugin.getServer().getPlayer(playerName);

        if (this.plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "adventure/sniper_duel")) {
            if (isOnCooldown(player.getUniqueId(), cooldowns)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.f, 1.f);

                double timeLeft = roundDecimalNumber((cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()), 1) / 1000;
            } else if (!isOnCooldown(player.getUniqueId(), cooldowns)) {
                //setCooldown(player.getUniqueId(), cooldowns, 60);

                StringBuilder displayableList = new StringBuilder();
                for (Player playerCheck : this.plugin.getServer().getOnlinePlayers()) {
                    if (playerCheck != player && player.getLocation().distance(playerCheck.getLocation()) <= 50) {
                        affectedPlayers.add(playerCheck);
                        applyEffects(playerCheck);
                        PotionEffect strength = new PotionEffect(PotionEffectType.STRENGTH, 200, 1, false, false, true);
                        player.addPotionEffect(strength);

                        displayableList.append(ChatColor.AQUA + playerCheck.getName()).append(ChatColor.GREEN + ", ");
                    }
                }

                if (affectedPlayers.size() > 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.f, 1.f);

                    displayableList.setLength(displayableList.length() - 2);
                    //   ItsKazzle, jkitsaustin, myutsu
                    if (displayableList.toString().split(", ").length > 1) {
                        displayableList.insert(displayableList.length() - affectedPlayers.getLast().getName().length(), ChatColor.GREEN + " and " + ChatColor.AQUA);
                    }

                    player.sendActionBar(ChatColor.GREEN + "affected " + displayableList + "!");
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.5f);

                    player.sendActionBar(ChatColor.RED + "No players were affected.");
                }

                setCooldown(player.getUniqueId(), cooldowns, 120);
                affectedPlayers.clear();
                displayableList.setLength(0);
            }
        }
    }

    private void applyEffects(Player player) {
        // Apply the glowing effect for 10 seconds (200 ticks)
        PotionEffect glowing = new PotionEffect(PotionEffectType.GLOWING, 200, 1, false, false, true);
        PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 200, 3, false, false, true);
        player.addPotionEffect(glowing);
        player.addPotionEffect(weakness);
    }
}
