package kazzleinc.simples5;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class UpsideDownWorldSetupClass {

    SimpleS5 plugin;

    public UpsideDownWorldSetupClass(SimpleS5 plugin) {
        this.plugin = plugin;
    }

    public void createUpsideDownWorld() throws IOException {
        World originalWorld = Bukkit.getWorld("world"); // The default main world is often "world"
        if (originalWorld == null) {
            plugin.getLogger().severe("Original world not found!");
            return;
        }

        // Get the directories for the original and new worlds
        File originalWorldFolder = originalWorld.getWorldFolder();
        File upsideDownWorldFolder = new File(plugin.getServer().getWorldContainer(), "upside-down");

        // Check if the world already exists to prevent overwriting
        if (upsideDownWorldFolder.exists()) {
            plugin.getLogger().info("Upside-down world already exists. Skipping creation.");
            return;
        }

        // Copy the original world folder to the new world folder
        copyWorldDirectory(originalWorldFolder.toPath(), upsideDownWorldFolder.toPath());

        // Create and load the new world
        WorldCreator worldCreator = new WorldCreator("upside-down");
        worldCreator.copy(originalWorld); // Copy settings like generator, seed, etc.
        Bukkit.createWorld(worldCreator);

        plugin.getLogger().info("Upside-down world created and loaded successfully.");
    }

    // Method to copy directories using Java's NIO
    private void copyWorldDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(path -> {
            try {
                Path targetPath = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
