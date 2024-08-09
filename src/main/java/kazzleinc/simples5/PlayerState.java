package kazzleinc.simples5;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlayerState {
    private double health;
    private int foodLevel;
    private float saturation;
    private float exhaustion;
    private Location location;
    private float yaw;
    private float pitch;

    public PlayerState(Player player) {
        this.health = player.getHealth();
        this.foodLevel = player.getFoodLevel();
        this.saturation = player.getSaturation();
        this.exhaustion = player.getExhaustion();
        this.location = player.getLocation().clone();
        this.yaw = player.getLocation().getYaw();
        this.pitch = player.getLocation().getPitch();
    }

    public void apply(Player player) {
        player.setHealth(this.health);
        player.setFoodLevel(this.foodLevel);
        player.setSaturation(this.saturation);
        player.setExhaustion(this.exhaustion);
        Location newLocation = this.location.clone();
        newLocation.setYaw(this.yaw);
        newLocation.setPitch(this.pitch);
        player.teleport(newLocation);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.f);
    }

    public Location getRewindLocation() {
        return this.location;
    }
}
