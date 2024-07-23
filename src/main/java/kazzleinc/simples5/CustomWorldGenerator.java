package kazzleinc.simples5;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.World;
import org.bukkit.generator.WorldInfo;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class CustomWorldGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData chunkData = createChunkData(world);
        return chunkData; // Return an empty chunk (void)
    }


    //custom world gen
    public static void createVoidWorld(JavaPlugin plugin, String worldName) {
        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generator(new CustomWorldGenerator());
        World world = plugin.getServer().createWorld(worldCreator);
        if (world != null) {
            world.setSpawnLocation(new Location(world, 0, 64, 0)); // Set the spawn location to Y=64
            world.getBlockAt(0, 64, 0).setType(Material.BEDROCK); // Optional: place a bedrock block at the spawn
        }
    }
}
