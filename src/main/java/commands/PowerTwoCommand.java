package commands;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

public class PowerTwoCommand implements CommandExecutor {
    SimpleS5 plugin;

    public List<String> enabledKeys = new ArrayList<>();

    public PowerTwoCommand(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            String playerName = plugin.provider.getInfo(player).getName();
            if (this.plugin.getConfig().getConfigurationSection("players." + plugin.provider.getInfo(player).getName() + ".powers") != null) {
                if (!enabledKeys.isEmpty()) {
                    enabledKeys.clear();
                }

                for (String keys : this.plugin.getConfig().getConfigurationSection("players." + plugin.provider.getInfo(player).getName() + ".powers").getKeys(false)) {
                    String key = keys;
                    Boolean value = this.plugin.getConfig().getBoolean("players." + plugin.provider.getInfo(player).getName() + ".powers." + key);

                    if (value) {
                        enabledKeys.add(key);
                    }
                }

                if (!enabledKeys.isEmpty()) {
                    switch (enabledKeys.get(this.plugin.getConfig().getInt("players." + playerName + ".mode"))) {
                        case "adventure/very_very_frightening":
                            plugin.vvfClass.action(playerName);
                            break;
                        case "nether/all_effects":
                            plugin.hdwghClass.action(playerName);
                            break;
                        case "husbandry/complete_catalogue":
                            plugin.catalogueClass.action(playerName);
                            break;
                        case "adventure/kill_all_mobs":
                            plugin.monstersClass.action(playerName);
                            break;
                        case "adventure/sniper_duel":
                            plugin.sniperDuelClass.action(playerName);
                        case "nether/uneasy_alliance":
                            plugin.uneasyAllianceClass.action(playerName);
                            break;
                        case "husbandry/froglights":
                            plugin.wopcClass.action(playerName);
                            break;
                        case "adventure/summon_iron_golem":
                            plugin.hiredHelpClass.action(playerName);
                        case "nether/ride_strider_in_overworld_lava":
                            plugin.feelsLikeHomeClass.action(playerName);
                            break;
                        case "nether/create_full_beacon":
                            plugin.beaconatorClass.action(playerName);
                            break;
                        case "husbandry/balanced_diet":
                            plugin.balancedDietClass.action(playerName);
                            break;
                        case "end/dragon_egg":
                            plugin.nextGenerationClass.action(playerName);
                            break;
                        case "adventure/bullseye":
                            plugin.bullseyeClass.action(playerName);
                            break;
                        case "events/event_power_one":
                            plugin.eventPowerOneClass.action(playerName);
                            break;
                        case "events/charlis_odyssey":
                            plugin.charliPowerClass.action(playerName);
                            break;
                        case "events/quaks_odyssey":
                            plugin.quakPowerClass.action(playerName);
                            break;
                        case "events/fortnite_odyssey":
                            plugin.fortniteOdysseyClass.action(playerName);
                            break;
                        case "adventure/trim_with_all_exclusive_armor_patterns":
                            plugin.scrollWheelTestClass.action(playerName);
                            break;
                        case "player/power_stolen":
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.f, 1.f);
                            player.sendMessage(ChatColor.RED + "Your power has been stolen, you can't use this!");
                            break;
                        default:
                            plugin.getLogger().warning("Power Use was called on player [" + playerName + "], but the power doesn't exist, the player doesn't have a power, or the power isn't initialized in the config.");
                            break;
                    }

                    this.plugin.updateCooldownDisplay();
                } else {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.f, 0.1f);
                    player.sendActionBar(ChatColor.RED + "You don't have any powers.");
                }

                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }

    }
}
