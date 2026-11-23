package kazzleinc.simples5;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PowerStoreTest {

    @TempDir
    File dataFolder;

    private PowerStore store;
    private Player kazzle;

    @BeforeEach
    void setUp() {
        TestServer.knownPlayers.clear();
        store = new PowerStore(TestServer.plugin(dataFolder));
        store.load();
        kazzle = TestServer.player("ItsKazzle");
    }

    private File dataFile() {
        return new File(dataFolder, "data.yml");
    }

    // ---------------------------------------------------------------- grant / drop

    @Test
    void newPlayerHasNoPowersSection() {
        assertFalse(store.hasPowersSection(kazzle));
        assertNull(store.getEnabledPowers(kazzle), "null means 'no powers entry', which callers rely on");
        assertEquals(0, store.getEnabledPowerCount(kazzle));
        assertTrue(store.getPowerKeys(kazzle).isEmpty());
    }

    @Test
    void grantingAPowerIsSavedStraightAway() {
        store.setPower(kazzle, "adventure/sniper_duel", true);

        assertTrue(store.hasPower(kazzle, "adventure/sniper_duel"));
        assertTrue(dataFile().exists(), "ownership changes must reach disk without waiting for the timer");

        PowerStore reloaded = new PowerStore(TestServer.plugin(dataFolder));
        reloaded.load();
        assertTrue(reloaded.hasPower(kazzle.getUniqueId(), "adventure/sniper_duel"));
    }

    @Test
    void droppedPowersKeepTheirKeyButAreNotEnabled() {
        store.setPower(kazzle, "adventure/sniper_duel", true);
        store.setPower(kazzle, "nether/all_effects", true);
        store.setPower(kazzle, "adventure/sniper_duel", false);

        assertEquals(List.of("nether/all_effects"), store.getEnabledPowers(kazzle));
        assertEquals(1, store.getEnabledPowerCount(kazzle));
        assertTrue(store.getPowerKeys(kazzle).contains("adventure/sniper_duel"));
        assertFalse(store.hasPower(kazzle, "adventure/sniper_duel"));
    }

    @Test
    void enabledPowersKeepTheOrderTheyWereGrantedIn() {
        // The selected slot (mode 0 / 1) indexes this list, so the order must be stable.
        store.setPower(kazzle, "nether/all_effects", true);
        store.setPower(kazzle, "adventure/sniper_duel", true);

        assertEquals(List.of("nether/all_effects", "adventure/sniper_duel"), store.getEnabledPowers(kazzle));
    }

    @Test
    void powersAreKeptPerPlayer() {
        Player other = TestServer.player("SomeoneElse");
        store.setPower(kazzle, "adventure/sniper_duel", true);

        assertFalse(store.hasPower(other, "adventure/sniper_duel"));
        assertNull(store.getEnabledPowers(other));
    }

    // ---------------------------------------------------------------- slot / mode

    @Test
    void modeDefaultsToZeroAndCanBeChanged() {
        assertFalse(store.hasMode(kazzle));
        assertEquals(0, store.getMode(kazzle));

        store.setMode(kazzle, 1);
        assertTrue(store.hasMode(kazzle));
        assertEquals(1, store.getMode(kazzle));
    }

    @Test
    void modeChangesAreWrittenOnShutdown() {
        store.setMode(kazzle, 1);
        assertFalse(dataFile().exists(), "slot changes wait for the 30-second flush");

        store.shutdown();

        PowerStore reloaded = new PowerStore(TestServer.plugin(dataFolder));
        reloaded.load();
        assertEquals(1, reloaded.getMode(kazzle));
    }

    // ---------------------------------------------------------------- global flags

    @Test
    void cappedAdvancementsAndMaceFlag() {
        assertFalse(store.isCapped("sniper_duel"));
        store.setCapped("sniper_duel", true);
        assertTrue(store.isCapped("sniper_duel"));

        assertFalse(store.isMaceCrafted());
        store.setMaceCrafted(true);
        assertTrue(store.isMaceCrafted());
    }

    @Test
    void powerTakenStatusCoversEveryDefaultPower() {
        Map<String, Boolean> before = store.getPowerTakenStatus();
        assertEquals(Boolean.FALSE, before.get("sniper_duel"));
        assertEquals(Boolean.FALSE, before.get("all_effects"));

        store.setPower(kazzle, "adventure/sniper_duel", true);

        Map<String, Boolean> after = store.getPowerTakenStatus();
        assertEquals(Boolean.TRUE, after.get("sniper_duel"));
        assertEquals(Boolean.FALSE, after.get("all_effects"));
    }

    // ---------------------------------------------------------------- loading

    @Test
    void unreadableDataFileIsMovedAsideNotOverwritten() throws Exception {
        Files.writeString(dataFile().toPath(), "players: [this is: not valid", StandardCharsets.UTF_8);

        store.load();

        assertFalse(dataFile().exists());
        File[] broken = dataFolder.listFiles((dir, name) -> name.startsWith("data.yml.broken-"));
        assertNotNull(broken);
        assertEquals(1, broken.length, "the bad file is kept so it can be fixed by hand");
        assertNull(store.getEnabledPowers(kazzle));
    }

    // ---------------------------------------------------------------- migration from config.yml

    @Test
    void migrationMovesNameKeyedDataToUuids() throws Exception {
        UUID kazzleId = kazzle.getUniqueId();
        TestServer.knownPlayers.put("ItsKazzle", TestServer.offlinePlayer("ItsKazzle", kazzleId));

        String oldConfig = String.join("\n",
                "players:",
                "  ItsKazzle:",
                "    mode: 1",
                "    powers:",
                "      adventure/sniper_duel: true",
                "      nether/all_effects: false",
                "  NeverJoined:",
                "    powers:",
                "      end/dragon_egg: true",
                "capped_advancements:",
                "  sniper_duel: true",
                "world:",
                "  mace_crafted: true",
                "");
        Files.writeString(new File(dataFolder, "config.yml").toPath(), oldConfig, StandardCharsets.UTF_8);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(dataFolder, "config.yml"));

        PowerStore migrating = new PowerStore(TestServer.plugin(dataFolder, config));
        migrating.load();
        migrating.migrateFromConfig();

        assertTrue(migrating.hasPower(kazzleId, "adventure/sniper_duel"));
        assertFalse(migrating.hasPower(kazzleId, "nether/all_effects"));
        assertEquals(1, migrating.getMode(kazzle));
        assertTrue(migrating.isCapped("sniper_duel"));
        assertTrue(migrating.isMaceCrafted());

        assertFalse(config.isSet("players"), "player data is removed from config.yml once data.yml is written");
        assertFalse(config.isSet("capped_advancements"));
        assertFalse(config.isSet("world"));

        YamlConfiguration written = YamlConfiguration.loadConfiguration(dataFile());
        assertTrue(written.isConfigurationSection("unmigrated.NeverJoined"),
                "players the server has never seen are kept, not dropped");
        assertEquals("ItsKazzle", written.getString("players." + kazzleId + ".name"));

        File[] backups = dataFolder.listFiles((dir, name) -> name.startsWith("config.yml.pre-5.10-backup-"));
        assertNotNull(backups);
        assertEquals(1, backups.length);
    }

    @Test
    void migrationDoesNothingWhenConfigHasNoPlayerData() {
        YamlConfiguration config = TestServer.bundledConfig();

        PowerStore migrating = new PowerStore(TestServer.plugin(dataFolder, config));
        migrating.load();
        migrating.migrateFromConfig();

        assertFalse(dataFile().exists());
        File[] backups = dataFolder.listFiles((dir, name) -> name.startsWith("config.yml.pre-5.10-backup-"));
        assertNotNull(backups);
        assertEquals(0, backups.length);
    }
}
