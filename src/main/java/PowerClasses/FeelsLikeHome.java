package PowerClasses;

import kazzleinc.simples5.SimpleS5;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FeelsLikeHome extends ParentPowerClass implements Listener {

    public FeelsLikeHome(SimpleS5 plugin) {
        super(plugin);
    }

    @Override
    public void action(String playerName) {

    }

    PotionEffect regenPot = new PotionEffect(PotionEffectType.REGENERATION, 40, 0, false, false, true);
    PotionEffect fireResPot = new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false, true);

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();;
        if (plugin.getConfig().getBoolean("players." + player.getName() + ".powers." + "ride_strider_in_overworld_lava")) {
            if (player.isVisualFire()) {
                player.addPotionEffect(regenPot);
            }

            player.addPotionEffect(fireResPot);
        }
    }

    public void removeFireResistance(Player player) {
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
    }


}
