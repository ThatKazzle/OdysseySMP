package kazzleinc.simples5;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Player data (owned powers, selected power slot, claimed advancements, world flags), stored in
 * {@code data.yml} and keyed by player UUID. {@code config.yml} holds only static settings and text.
 *
 * <p>Layout of data.yml:
 * <pre>
 * players:
 *   &lt;uuid&gt;:
 *     name: LastKnownName      # informational only, never used for lookups
 *     mode: 0                  # index into the player's enabled powers
 *     powers:
 *       adventure/sniper_duel: true
 * capped_advancements:
 *   sniper_duel: true
 * world:
 *   mace_crafted: true
 * </pre>
 *
 * <p>Saving: {@link #markDirty()} is for frequent, low-stakes changes (power slot) and is flushed by a
 * timer. {@link #saveSoon()} writes on the next tick, for changes that must survive a crash
 * (power granted, dropped, withdrawn, stolen), so that a crash can't duplicate or lose a power.
 */
public class PowerStore {

    private static final long FLUSH_INTERVAL_TICKS = 20L * 30;

    private final SimpleS5 plugin;
    private final File file;
    private YamlConfiguration data = new YamlConfiguration();

    private boolean dirty = false;
    private boolean saveScheduled = false;
    private long snapshotSeq = 0;
    private long writtenSeq = 0;
    private final Object writeLock = new Object();
    private BukkitTask flushTask;

    public PowerStore(SimpleS5 plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    // ---------------------------------------------------------------- lifecycle

    public void load() {
        data = new YamlConfiguration();
        if (file.exists()) {
            try {
                data.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                // Don't silently start empty and then overwrite the bad file with nothing.
                File broken = new File(file.getParentFile(), "data.yml.broken-" + timestamp());
                file.renameTo(broken);
                plugin.getLogger().log(Level.SEVERE, "Could not read data.yml; moved it to " + broken.getName() + " and started with empty data.", e);
                data = new YamlConfiguration();
            }
        }
    }

    public void startAutoFlush() {
        flushTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (dirty) writeAsync();
        }, FLUSH_INTERVAL_TICKS, FLUSH_INTERVAL_TICKS);
    }

    /** Blocking save; call from onDisable. */
    public void shutdown() {
        if (flushTask != null) flushTask.cancel();
        String snapshot = data.saveToString();
        long seq = ++snapshotSeq;
        dirty = false;
        write(snapshot, seq);
    }

    // ---------------------------------------------------------------- saving

    public void markDirty() {
        dirty = true;
    }

    public void saveSoon() {
        dirty = true;
        if (saveScheduled) return;
        saveScheduled = true;
        Bukkit.getScheduler().runTask(plugin, () -> {
            saveScheduled = false;
            if (dirty) writeAsync();
        });
    }

    private void writeAsync() {
        // Serialise on the main thread (Bukkit config objects aren't thread-safe), write off it.
        String snapshot = data.saveToString();
        long seq = ++snapshotSeq;
        dirty = false;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> write(snapshot, seq));
    }

    private void write(String snapshot, long seq) {
        synchronized (writeLock) {
            if (seq <= writtenSeq) return; // a newer snapshot was already written
            try {
                Files.createDirectories(file.getParentFile().toPath());
                Path tmp = new File(file.getParentFile(), "data.yml.tmp").toPath();
                Files.write(tmp, snapshot.getBytes(StandardCharsets.UTF_8));
                try {
                    Files.move(tmp, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException atomicFailed) {
                    Files.move(tmp, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                writtenSeq = seq;
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save data.yml", e);
                dirty = true; // try again on the next flush
            }
        }
    }

    // ---------------------------------------------------------------- per-player data

    private String base(UUID id) {
        return "players." + id;
    }

    /** Records the player's current name next to their UUID, so data.yml stays readable. */
    public void touchName(Player player) {
        String path = base(player.getUniqueId()) + ".name";
        if (!player.getName().equals(data.getString(path))) {
            data.set(path, player.getName());
            markDirty();
        }
    }

    public boolean hasPower(Player player, String powerKey) {
        return hasPower(player.getUniqueId(), powerKey);
    }

    public boolean hasPower(UUID id, String powerKey) {
        ConfigurationSection powers = data.getConfigurationSection(base(id) + ".powers");
        return powers != null && powers.getBoolean(powerKey, false);
    }

    /** Sets a power flag. Uses {@link #saveSoon()}, since ownership changes must survive a crash. */
    public void setPower(Player player, String powerKey, boolean value) {
        setPower(player.getUniqueId(), powerKey, value);
    }

    public void setPower(UUID id, String powerKey, boolean value) {
        // getConfigurationSection + set avoids '.' path splitting problems in keys like "adventure/x".
        ConfigurationSection powers = data.getConfigurationSection(base(id) + ".powers");
        if (powers == null) powers = data.createSection(base(id) + ".powers");
        powers.set(powerKey, value);
        saveSoon();
    }

    /** All power keys stored for the player, including ones set to false. Never null. */
    public Set<String> getPowerKeys(Player player) {
        ConfigurationSection powers = data.getConfigurationSection(base(player.getUniqueId()) + ".powers");
        return powers == null ? Collections.emptySet() : powers.getKeys(false);
    }

    /** Whether the player has any powers entry at all (old code returned null in that case). */
    public boolean hasPowersSection(Player player) {
        return data.getConfigurationSection(base(player.getUniqueId()) + ".powers") != null;
    }

    /** Enabled power keys, in stored order. Returns null if the player has no powers section, as before. */
    public ArrayList<String> getEnabledPowers(Player player) {
        ConfigurationSection powers = data.getConfigurationSection(base(player.getUniqueId()) + ".powers");
        if (powers == null) return null;

        ArrayList<String> enabled = new ArrayList<>();
        for (String key : powers.getKeys(false)) {
            if (powers.getBoolean(key, false)) enabled.add(key);
        }
        return enabled;
    }

    public int getEnabledPowerCount(Player player) {
        ArrayList<String> enabled = getEnabledPowers(player);
        return enabled == null ? 0 : enabled.size();
    }

    /** True if a mode has been stored for the player. */
    public boolean hasMode(Player player) {
        return data.contains(base(player.getUniqueId()) + ".mode");
    }

    public int getMode(Player player, int def) {
        return data.getInt(base(player.getUniqueId()) + ".mode", def);
    }

    public int getMode(Player player) {
        return getMode(player, 0);
    }

    public void setMode(Player player, int mode) {
        String path = base(player.getUniqueId()) + ".mode";
        if (!data.contains(path) || data.getInt(path) != mode) {
            data.set(path, mode);
            markDirty();
        }
    }

    // ---------------------------------------------------------------- global data

    public boolean isCapped(String advancementKey) {
        ConfigurationSection capped = data.getConfigurationSection("capped_advancements");
        return capped != null && capped.getBoolean(advancementKey, false);
    }

    public void setCapped(String advancementKey, boolean value) {
        ConfigurationSection capped = data.getConfigurationSection("capped_advancements");
        if (capped == null) capped = data.createSection("capped_advancements");
        capped.set(advancementKey, value);
        saveSoon();
    }

    public boolean isMaceCrafted() {
        return data.getBoolean("world.mace_crafted", false);
    }

    public void setMaceCrafted(boolean value) {
        data.set("world.mace_crafted", value);
        saveSoon();
    }

    /**
     * For each key under config.yml {@code defaults}, whether any player currently has that power.
     * Replaces the two identical checkPowerStatus() copies in SimpleS5 and odysseyCommands.
     */
    public Map<String, Boolean> getPowerTakenStatus() {
        Map<String, Boolean> status = new HashMap<>();
        ConfigurationSection defaults = plugin.getConfig().getConfigurationSection("defaults");
        if (defaults == null) return status;

        Set<String> defaultKeys = defaults.getKeys(false);
        for (String key : defaultKeys) status.put(key, false);

        ConfigurationSection players = data.getConfigurationSection("players");
        if (players == null) return status;

        for (String uuid : players.getKeys(false)) {
            ConfigurationSection powers = players.getConfigurationSection(uuid + ".powers");
            if (powers == null) continue;
            for (String key : defaultKeys) {
                String powerPath = defaults.getString(key);
                if (powerPath != null && powers.getBoolean(powerPath, false)) status.put(key, true);
            }
        }
        return status;
    }

    /** "uuid/powerKey" -> value for every stored power. Used only for debugging output. */
    public Map<String, Boolean> getAllPowers() {
        Map<String, Boolean> all = new HashMap<>();
        ConfigurationSection players = data.getConfigurationSection("players");
        if (players == null) return all;
        for (String uuid : players.getKeys(false)) {
            ConfigurationSection powers = players.getConfigurationSection(uuid + ".powers");
            if (powers == null) continue;
            for (String key : powers.getKeys(false)) all.put(uuid + "/" + key, powers.getBoolean(key));
        }
        return all;
    }

    // ---------------------------------------------------------------- migration from config.yml

    /**
     * One-time move of player data from config.yml (keyed by name) into data.yml (keyed by UUID).
     * Runs only if config.yml still has a "players", "capped_advancements" or "world" section.
     * Backs up config.yml first. Names that can't be matched to a player who has joined this server
     * are kept under "unmigrated" in data.yml, so nothing is lost and they can be fixed by hand.
     */
    public void migrateFromConfig() {
        FileConfiguration config = plugin.getConfig();
        boolean hasPlayers = config.isConfigurationSection("players");
        boolean hasCapped = config.isConfigurationSection("capped_advancements");
        boolean hasWorld = config.isConfigurationSection("world");
        if (!hasPlayers && !hasCapped && !hasWorld) return;

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File backup = new File(plugin.getDataFolder(), "config.yml.pre-5.10-backup-" + timestamp());
        try {
            if (configFile.exists()) Files.copy(configFile.toPath(), backup.toPath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not back up config.yml; skipping data migration so nothing is lost. Fix the problem and restart.", e);
            return;
        }
        plugin.getLogger().info("Migrating player data from config.yml to data.yml (backup: " + backup.getName() + ")");

        int moved = 0;
        int unmatched = 0;

        if (hasPlayers) {
            ConfigurationSection players = config.getConfigurationSection("players");
            for (String name : players.getKeys(false)) {
                ConfigurationSection oldEntry = players.getConfigurationSection(name);
                if (oldEntry == null) continue;

                OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(name);
                if (offline == null) {
                    copySection(oldEntry, data.createSection("unmigrated." + name));
                    plugin.getLogger().warning("  " + name + ": no player by that name has joined this server; kept under 'unmigrated' in data.yml");
                    unmatched++;
                    continue;
                }

                UUID id = offline.getUniqueId();
                ConfigurationSection target = data.getConfigurationSection(base(id));
                if (target == null) target = data.createSection(base(id));
                copySection(oldEntry, target);
                target.set("name", offline.getName() != null ? offline.getName() : name);

                plugin.getLogger().info("  " + name + " -> " + id + " " + describePowers(oldEntry));
                moved++;
            }
            config.set("players", null);
        }

        if (hasCapped) {
            ConfigurationSection capped = config.getConfigurationSection("capped_advancements");
            ConfigurationSection target = data.getConfigurationSection("capped_advancements");
            if (target == null) target = data.createSection("capped_advancements");
            copySection(capped, target);
            config.set("capped_advancements", null);
            plugin.getLogger().info("  capped_advancements: " + capped.getKeys(false).size() + " entries");
        }

        if (hasWorld) {
            ConfigurationSection world = config.getConfigurationSection("world");
            ConfigurationSection target = data.getConfigurationSection("world");
            if (target == null) target = data.createSection("world");
            copySection(world, target);
            config.set("world", null);
            plugin.getLogger().info("  world: " + world.getKeys(false));
        }

        // Write data.yml before removing anything from config.yml.
        String snapshot = data.saveToString();
        long seq = ++snapshotSeq;
        write(snapshot, seq);
        if (writtenSeq != seq) {
            plugin.getLogger().severe("Could not write data.yml; config.yml left unchanged. Player data stays in config.yml until this is fixed.");
            plugin.reloadConfig();
            return;
        }
        plugin.saveConfig();

        plugin.getLogger().info("Migration done: " + moved + " players moved, " + unmatched + " kept under 'unmigrated'.");
    }

    private static void copySection(ConfigurationSection from, ConfigurationSection to) {
        for (String key : from.getKeys(false)) {
            if (from.isConfigurationSection(key)) {
                ConfigurationSection child = to.getConfigurationSection(key);
                if (child == null) child = to.createSection(key);
                copySection(from.getConfigurationSection(key), child);
            } else {
                to.set(key, from.get(key));
            }
        }
    }

    private static String describePowers(ConfigurationSection entry) {
        ConfigurationSection powers = entry.getConfigurationSection("powers");
        if (powers == null) return "(no powers)";
        List<String> on = new ArrayList<>();
        for (String key : powers.getKeys(false)) if (powers.getBoolean(key)) on.add(key);
        return on.isEmpty() ? "(no active powers)" : on.toString();
    }

    private static String timestamp() {
        return new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    }
}
