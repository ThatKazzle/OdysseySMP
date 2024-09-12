package kazzleinc.simples5;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FollowParticle extends BukkitRunnable {

    private SimpleS5 plugin;
    private final Location start;
    private final Location target;
    private Vector velocity;
    private double speedIncrement = 0.02; // How much to increase speed each tick
    private double maxSpeed = 1.0; // Maximum speed

    public FollowParticle(Location start, Location target, SimpleS5 plugin) {
        this.start = start.clone();
        this.target = target.clone();
        this.plugin = plugin;
        // Calculate the initial velocity vector from start to target
        Vector direction = target.toVector().subtract(start.toVector()).normalize();
        this.velocity = direction.multiply(0.1); // Start with a low speed
    }

    @Override
    public void run() {
        // If the particle is close to the target, stop the task
        if (start.distance(target) < 0.5) {
            this.cancel();
            return;
        }

        Bukkit.getOnlinePlayers().stream().findFirst().get().sendMessage("UPDATE!");

        // Spawn a particle at the current location
        start.getWorld().spawnParticle(Particle.CRIT, start, 1, 0, 0, 0, 0);

        // Gradually increase the velocity's magnitude
        double currentSpeed = velocity.length();
        if (currentSpeed < maxSpeed) {
            velocity = velocity.normalize().multiply(currentSpeed + speedIncrement);
        }

        // Update the particle's location using the velocity
        start.add(velocity);
    }
}