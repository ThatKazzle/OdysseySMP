package kazzleinc.simples5;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A fake Bukkit server for unit tests. Scheduled tasks run straight away, so data.yml is written
 * before the test checks it. Only what PowerStore and the helpers use is faked.
 */
final class TestServer {

    /** Players that have "joined" the fake server, by name, for Bukkit.getOfflinePlayerIfCached. */
    static final Map<String, OfflinePlayer> knownPlayers = new HashMap<>();

    private static boolean installed = false;

    private TestServer() {
    }

    static synchronized void install() {
        if (installed) return;

        Server server = mock(Server.class);
        when(server.getLogger()).thenReturn(Logger.getLogger("TestServer"));

        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        BukkitTask task = mock(BukkitTask.class);
        when(scheduler.runTask(any(Plugin.class), any(Runnable.class))).thenAnswer(call -> {
            call.getArgument(1, Runnable.class).run();
            return task;
        });
        when(scheduler.runTaskAsynchronously(any(Plugin.class), any(Runnable.class))).thenAnswer(call -> {
            call.getArgument(1, Runnable.class).run();
            return task;
        });
        when(scheduler.runTaskTimer(any(Plugin.class), any(Runnable.class), anyLong(), anyLong())).thenReturn(task);
        when(server.getScheduler()).thenReturn(scheduler);

        when(server.getOfflinePlayerIfCached(anyString())).thenAnswer(call -> knownPlayers.get(call.getArgument(0, String.class)));

        try {
            // Bukkit.setServer also prints version info that needs a real server, so set the field directly.
            Field field = Bukkit.class.getDeclaredField("server");
            field.setAccessible(true);
            field.set(null, server);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not install the test server", e);
        }
        installed = true;
    }

    /** A plugin whose data folder is {@code dataFolder} and whose config is the jar's config.yml. */
    static SimpleS5 plugin(File dataFolder) {
        return plugin(dataFolder, bundledConfig());
    }

    static SimpleS5 plugin(File dataFolder, YamlConfiguration config) {
        install();
        SimpleS5 plugin = mock(SimpleS5.class);
        when(plugin.getDataFolder()).thenReturn(dataFolder);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("SimpleS5-test"));
        when(plugin.getConfig()).thenReturn(config);
        return plugin;
    }

    static YamlConfiguration bundledConfig() {
        try (InputStreamReader reader = new InputStreamReader(
                TestServer.class.getResourceAsStream("/config.yml"), StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            throw new IllegalStateException("Could not read config.yml", e);
        }
    }

    static Player player(String name) {
        Player player = mock(Player.class);
        UUID id = UUID.nameUUIDFromBytes(("player:" + name).getBytes(StandardCharsets.UTF_8));
        when(player.getUniqueId()).thenReturn(id);
        when(player.getName()).thenReturn(name);
        return player;
    }

    static OfflinePlayer offlinePlayer(String name, UUID id) {
        OfflinePlayer offline = mock(OfflinePlayer.class);
        when(offline.getUniqueId()).thenReturn(id);
        when(offline.getName()).thenReturn(name);
        return offline;
    }
}
