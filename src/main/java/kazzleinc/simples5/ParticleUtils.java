package kazzleinc.simples5;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedParticle;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ParticleUtils {

    /**
     * Creates a ring of particles at a specified location.
     *
     * @param center The center location of the ring.
     * @param radius The radius of the ring.
     * @param density The number of particles to generate in the ring.
     * @param particle The type of particle to generate.
     * @param color The color of the particle (only applies if particle is REDSTONE).
     */
    public static void createParticleRing(Location center, double radius, int density, Particle particle, Color color, int size) {
        // Get the world from the location
        World world = center.getWorld();

        // Calculate the angle step based on the density
        double angleStep = 2 * Math.PI / density;

        // Create DustOptions for the particle if needed
        Particle.DustOptions dustOptions = null;
        if (particle == Particle.DUST) {
            dustOptions = new Particle.DustOptions(color, size);
        }

        // Generate particles in a circle
        for (int i = 0; i < density; i++) {
            double angle = i * angleStep;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLocation = new Location(world, x, center.getY(), z);

            // Spawn the particle
            if (particle == Particle.DUST && dustOptions != null) {
                world.spawnParticle(particle, particleLocation, 1, dustOptions);
            } else {
                world.spawnParticle(particle, particleLocation, 1);
            }
        }
    }

    /**
     * Creates a dodecahedron of particles at a specified location.
     *
     * @param center The center location of the dodecahedron.
     * @param size The size (edge length) of the dodecahedron.
     * @param density The number of particles per edge.
     * @param particle The type of particle to generate.
     * @param color The color of the particle (only applies if particle is DUST).
     */
    public static void createParticleDodecahedron(Location center, double size, int density, Particle particle, Color color) {
        World world = center.getWorld();

        // Precompute some constants
        double phi = (1 + Math.sqrt(5)) / 2; // The golden ratio
        double a = 1.0;
        double b = 1.0 / phi;
        double c = 2.0 - phi;

        // Vertices of a dodecahedron centered at the origin
        Vector[] vertices = new Vector[]{
                new Vector(a, a, a), new Vector(a, a, -a), new Vector(a, -a, a), new Vector(a, -a, -a),
                new Vector(-a, a, a), new Vector(-a, a, -a), new Vector(-a, -a, a), new Vector(-a, -a, -a),
                new Vector(b, c, 0), new Vector(-b, c, 0), new Vector(b, -c, 0), new Vector(-b, -c, 0),
                new Vector(c, 0, b), new Vector(c, 0, -b), new Vector(-c, 0, b), new Vector(-c, 0, -b),
                new Vector(0, b, c), new Vector(0, -b, c), new Vector(0, b, -c), new Vector(0, -b, -c)
        };

        // Scale and translate vertices to the center location
        for (Vector vertex : vertices) {
            vertex.multiply(size);
            vertex.add(center.toVector());
        }

        // Edges of a dodecahedron (pairs of vertex indices)
        int[][] edges = new int[][]{
                {0, 8}, {0, 9}, {0, 12}, {0, 16}, {0, 17},
                {1, 8}, {1, 9}, {1, 13}, {1, 18}, {1, 19},
                {2, 10}, {2, 11}, {2, 12}, {2, 16}, {2, 17},
                {3, 10}, {3, 11}, {3, 13}, {3, 18}, {3, 19},
                {4, 8}, {4, 9}, {4, 14}, {4, 16}, {4, 17},
                {5, 8}, {5, 9}, {5, 15}, {5, 18}, {5, 19},
                {6, 10}, {6, 11}, {6, 14}, {6, 16}, {6, 17},
                {7, 10}, {7, 11}, {7, 15}, {7, 18}, {7, 19},
                {12, 13}, {14, 15}, {16, 18}, {17, 19}, {10, 12},
                {11, 13}, {8, 14}, {9, 15}, {0, 4}, {1, 5},
                {2, 6}, {3, 7}
        };

        // Create DustOptions for the particle if needed
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1);

        // Generate particles along the edges
        for (int[] edge : edges) {
            Vector start = vertices[edge[0]];
            Vector end = vertices[edge[1]];
            Vector step = end.clone().subtract(start).multiply(1.0 / density);

            for (int i = 0; i <= density; i++) {
                Vector point = start.clone().add(step.clone().multiply(i));
                Location particleLocation = point.toLocation(world);
                world.spawnParticle(particle, particleLocation, 1, dustOptions);
            }
        }
    }

    /**
     * Creates a dodecahedron of particles at a specified location.
     *
     * @param center The center location of the dodecahedron.
     * @param size The size (edge length) of the dodecahedron.
     * @param density The number of particles per edge.
     * @param particle The type of particle to generate.
     * @param color The color of the particle (only applies if particle is DUST).
     */
    public static void createParticleRealDodecahedron(Location center, double size, int density, Particle particle, Color color) {
        World world = center.getWorld();

        // Golden ratio
        double phi = (1 + Math.sqrt(5)) / 2;
        double a = size;
        double b = size / phi;

        // Vertices of a dodecahedron centered at the origin
        Vector[] vertices = new Vector[] {
                new Vector( a,  a,  a), new Vector( a,  a, -a), new Vector( a, -a,  a), new Vector( a, -a, -a),
                new Vector(-a,  a,  a), new Vector(-a,  a, -a), new Vector(-a, -a,  a), new Vector(-a, -a, -a),
                new Vector( 0,  b,  a/b), new Vector( 0,  b, -a/b), new Vector( 0, -b,  a/b), new Vector( 0, -b, -a/b),
                new Vector( b,  a/b, 0), new Vector( b, -a/b, 0), new Vector(-b,  a/b, 0), new Vector(-b, -a/b, 0),
                new Vector( a/b, 0,  b), new Vector( a/b, 0, -b), new Vector(-a/b, 0,  b), new Vector(-a/b, 0, -b)
        };

        // Translate vertices to the center location
        for (Vector vertex : vertices) {
            vertex.add(center.toVector());
        }

        // Edges of a dodecahedron (pairs of vertex indices)
        int[][] edges = new int[][] {
                { 0,  8}, { 0, 10}, { 0, 16}, { 0, 12}, { 0, 14},
                { 1,  9}, { 1, 11}, { 1, 17}, { 1, 13}, { 1, 15},
                { 2,  8}, { 2, 10}, { 2, 16}, { 2, 12}, { 2, 14},
                { 3,  9}, { 3, 11}, { 3, 17}, { 3, 13}, { 3, 15},
                { 4,  8}, { 4, 10}, { 4, 18}, { 4, 12}, { 4, 14},
                { 5,  9}, { 5, 11}, { 5, 19}, { 5, 13}, { 5, 15},
                { 6, 10}, { 6, 12}, { 6, 18}, { 6, 14}, { 6, 16},
                { 7, 11}, { 7, 13}, { 7, 19}, { 7, 15}, { 7, 17},
                { 8, 16}, { 9, 17}, {10, 18}, {11, 19}, {12, 16},
                {13, 17}, {14, 18}, {15, 19}
        };

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1);

        // Generate particles along the edges
        for (int[] edge : edges) {
            Vector start = vertices[edge[0]];
            Vector end = vertices[edge[1]];
            Vector step = end.clone().subtract(start).multiply(1.0 / density);

            for (int i = 0; i <= density; i++) {
                Vector point = start.clone().add(step.clone().multiply(i));
                Location particleLocation = point.toLocation(world);
                world.spawnParticle(particle, particleLocation, 1, dustOptions);
            }
        }
    }

    /**
     * Creates a sphere of particles at a specified location.
     *
     * @param center   The center location of the sphere.
     * @param radius   The radius of the sphere.
     * @param density  The density of particles on the surface of the sphere.
     * @param particle The type of particle to generate.
     * @param color    The color of the particle (only applies if particle is DUST).
     */
    public static void createParticleSphere(Location center, double radius, int density, Particle particle, Color color, int size) {
        World world = center.getWorld();
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, size);

        double phiStep = Math.PI / density;
        double thetaStep = 2 * Math.PI / density;

        for (int i = 0; i <= density; i++) {
            double phi = i * phiStep;
            for (int j = 0; j <= density; j++) {
                double theta = j * thetaStep;
                double x = center.getX() + radius * Math.sin(phi) * Math.cos(theta);
                double y = center.getY() + radius * Math.cos(phi);
                double z = center.getZ() + radius * Math.sin(phi) * Math.sin(theta);
                Location particleLocation = new Location(world, x, y, z);
                world.spawnParticle(particle, particleLocation, 1, dustOptions);
            }
        }
    }

    /**
     * Creates a rotating sphere of particles at a specified location.
     *
     * @param center The center location of the sphere.
     * @param radius The radius of the sphere.
     * @param density The density of particles on the surface of the sphere.
     * @param particle The type of particle to generate.
     * @param color The color of the particle (only applies if particle is DUST).
     * @param angle The angle of rotation in degrees.
     */
    public static void createRotatingParticleSphere(Location center, double radius, int density, Particle particle, Color color, double angle) {
        World world = center.getWorld();
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1);

        double phiStep = Math.PI / density;
        double thetaStep = 2 * Math.PI / density;
        double rotationAngle = Math.toRadians(angle);

        for (int i = 0; i <= density; i++) {
            double phi = i * phiStep;
            for (int j = 0; j <= density; j++) {
                double theta = j * thetaStep;
                double x = radius * Math.sin(phi) * Math.cos(theta);
                double y = radius * Math.cos(phi);
                double z = radius * Math.sin(phi) * Math.sin(theta);

                // Apply rotation around Y-axis
                double rotatedX = x * Math.cos(rotationAngle) - z * Math.sin(rotationAngle);
                double rotatedZ = x * Math.sin(rotationAngle) + z * Math.cos(rotationAngle);

                Location particleLocation = center.clone().add(rotatedX, y, rotatedZ);
                world.spawnParticle(particle, particleLocation, 1, dustOptions);
            }
        }
    }

    public static void createParticleLine(Location start, Location end, int density, Particle.DustOptions dustOptions) {
        if (!start.getWorld().equals(end.getWorld())) {
            throw new IllegalArgumentException("Start and end locations must be in the same world.");
        }

        World world = start.getWorld();
        Vector startVector = start.toVector();
        Vector endVector = end.toVector();
        Vector direction = endVector.clone().subtract(startVector);
        double length = direction.length();
        direction.normalize();

        double interval = length / density;
        Vector step = direction.multiply(interval);

        for (int i = 0; i <= density; i++) {
            Vector currentPosition = startVector.clone().add(step.clone().multiply(i));
            world.spawnParticle(Particle.DUST, currentPosition.toLocation(world), 1, dustOptions);
        }
    }

    public static void createWardenLine(Location start, Location end, int density, Particle.DustOptions dustOptions) {
        if (!start.getWorld().equals(end.getWorld())) {
            throw new IllegalArgumentException("Start and end locations must be in the same world.");
        }

        World world = start.getWorld();
        Vector startVector = start.toVector();
        Vector endVector = end.toVector();
        Vector direction = endVector.clone().subtract(startVector);
        double length = direction.length();
        direction.normalize();

        double interval = length / density;
        Vector step = direction.multiply(interval);

        for (int i = 0; i <= density; i++) {
            Vector currentPosition = startVector.clone().add(step.clone().multiply(i));
            world.spawnParticle(Particle.DUST, currentPosition.toLocation(world), 1, dustOptions);
        }
    }

    public static void createParticleRandomLine(Location start, Location end, int density, int size) {
        if (!start.getWorld().equals(end.getWorld())) {
            throw new IllegalArgumentException("Start and end locations must be in the same world.");
        }

        World world = start.getWorld();
        Vector startVector = start.toVector();
        Vector endVector = end.toVector();
        Vector direction = endVector.clone().subtract(startVector);
        double length = direction.length();
        direction.normalize();

        double interval = length / density;
        Vector step = direction.multiply(interval);

        for (int i = 0; i <= density; i++) {
            Vector currentPosition = startVector.clone().add(step.clone().multiply(i));
            world.spawnParticle(Particle.DUST, currentPosition.toLocation(world), 1, new Particle.DustOptions(Color.fromRGB(RandomUtils.getRandomIntInRange(0, 255), RandomUtils.getRandomIntInRange(0, 255), RandomUtils.getRandomIntInRange(0, 255)), size));
        }
    }
}
