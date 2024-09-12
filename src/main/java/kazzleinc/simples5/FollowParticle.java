package kazzleinc.simples5;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class FollowParticle extends BukkitRunnable {

    private SimpleS5 plugin;
    private final Location start;
    private final Location target;
    private Vector velocity;
    private double speedIncrement = 0.02; // How much to increase speed each tick
    private double maxSpeed = 0.4; // Maximum speed
    private double easingFactor = 0.05; // How much influence the target has on the velocity each tick

    public FollowParticle(Location start, Location target, SimpleS5 plugin) {
        this.start = start.clone();
        this.target = target.clone();
        this.plugin = plugin;

        Random random = new Random();
        double angle = random.nextDouble() * 360; // Angle between 0 and 360 degrees

        // Convert the angle to radians for trigonometric functions
        double radians = Math.toRadians(angle);

        // Set an initial speed for the arc
        double initialSpeed = 0.5;

        // Calculate the initial velocity components based on the random angle
        double xVelocity = initialSpeed * Math.cos(radians); // Horizontal X velocity
        double zVelocity = initialSpeed * Math.sin(radians); // Horizontal Z velocity
        double yVelocity = random.nextDouble(-0.4, 0.4); // Upward velocity (higher to give it an arc)

        // Initialize the velocity vector
        this.velocity = new Vector(xVelocity, yVelocity, zVelocity);

        // Calculate the initial velocity vector from start to target
        //Vector direction = target.toVector().subtract(start.toVector()).normalize();
        //this.velocity = direction.multiply(0.1); // Start with a low speed
    }

    @Override
    public void run() {
        // If the particle is close to the target, stop the task
        if (start.distance(target) < 0.3) {
            this.cancel();
            return;
        }

        //Bukkit.getOnlinePlayers().stream().findFirst().get().sendMessage("UPDATE!");

        // Spawn a particle at the current location
        start.getWorld().spawnParticle(Particle.DUST, start, 1, new Particle.DustOptions(Color.RED, 2));

        // Calculate the direction towards the target
        Vector toTarget = target.toVector().subtract(start.toVector()).normalize();

        // Apply easing: slowly adjust the velocity toward the target
        velocity.add(toTarget.multiply(easingFactor));

        // Update the particle's position using the velocity
        // Gradually increase the velocity's magnitude
        double currentSpeed = velocity.length();
        if (currentSpeed < maxSpeed) {
            velocity = velocity.normalize().multiply(currentSpeed + speedIncrement);
        }
        easingFactor += 0.025;

        start.add(velocity);
    }
}