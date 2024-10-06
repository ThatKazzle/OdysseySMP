package kazzleinc.simples5;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Random;

public class FollowParticle extends BukkitRunnable {

    private SimpleS5 plugin;
    private final Location start;
    private final Location target;
    private Vector velocity;
    private double speedIncrement = 0.0004; // How much to increase speed each tick
    private double maxSpeed = 0.005; // Maximum speed
    private double easingFactor = 0.08; // How much influence the target has on the velocity each tick
    private Player owner;

    public FollowParticle(Location start, Vector startDirection, Location target, Player owner, SimpleS5 plugin) {
        this.owner = owner;
        this.start = start.clone();
        this.target = target.clone();
        this.plugin = plugin;

        Vector forward = startDirection.clone().normalize();

        Random random = new Random();
        double angle = random.nextDouble() * 360; // Angle between 0 and 360 degrees

        // Convert the angle to radians for trigonometric functions
        double radians = Math.toRadians(angle);

        // Set an initial speed for the arc
        double initialSpeed = 1;

        // Calculate the initial velocity components based on the random angle
        double xVelocity = initialSpeed * Math.cos(radians); // Horizontal X velocity
        double zVelocity = initialSpeed * Math.sin(radians); // Horizontal Z velocity
        double yVelocity = random.nextDouble(-0.4, 0.4); // Upward velocity (higher to give it an arc)

        Vector randomDirection = new Vector(xVelocity, yVelocity, zVelocity);

        if (forward.dot(randomDirection) < 0) {
            randomDirection.setX(-randomDirection.getX());
            randomDirection.setZ(-randomDirection.getZ());
        }

        // Combine the forward direction with the random direction to get a varied initial velocity
        this.velocity = forward.clone().add(randomDirection.multiply(100)).normalize().multiply(initialSpeed);
    }

    @Override
    public void run() {
        // If the particle is close to the target, stop the task
        if (start.distance(target) < 0.7) {
            this.cancel();
            return;
        } else {
            //Bukkit.getPlayer("ItsKazzle").sendMessage(String.valueOf(start.distance(target)));
        }

        for (Entity entity : start.getNearbyEntities(0.4, 0.4, 0.4)) {
            if (entity instanceof LivingEntity) {
                if (entity != this.owner) {
                    LivingEntity livEnt = (LivingEntity) entity;
                    livEnt.setHealth(livEnt.getHealth() - 0.35);
                    livEnt.damage(0.01);
                    livEnt.setNoDamageTicks(0);

                    livEnt.getWorld().spawnParticle(Particle.SONIC_BOOM, livEnt.getLocation(), 1);
                }
            }
        }

        //Bukkit.getOnlinePlayers().stream().findFirst().get().sendMessage("UPDATE!");

        // Spawn a particle at the current location
        start.getWorld().spawnParticle(Particle.DUST, start, 5, new Particle.DustOptions(Color.AQUA, 1));

        // Calculate the direction towards the target
        Vector toTarget = target.clone().toVector().subtract(start.toVector()).normalize();

        // Apply easing: slowly adjust the velocity toward the target
        velocity.add(toTarget.multiply(easingFactor));

        velocity.multiply(0.9);

        double currentSpeed = velocity.length();
        easingFactor += 0.001;

        if (easingFactor > 1) {
            this.cancel();
        }

        start.add(velocity);
    }
}