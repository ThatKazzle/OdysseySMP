package kazzleinc.simples5;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import powers.ParentPowerClass;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

class PowerRegistryTest {

    private static ParentPowerClass dummyPower() {
        return new ParentPowerClass(null) {
            @Override
            public void action(Player player) {
            }
        };
    }

    @Test
    void shortKeyDropsTheAdvancementTab() {
        assertEquals("sniper_duel", PowerRegistry.shortKey("adventure/sniper_duel"));
        assertEquals("sniper_duel", PowerRegistry.shortKey("sniper_duel"));
    }

    @Test
    void titleCaseCapitalisesEachWord() {
        assertEquals("Very Very Frightening", PowerRegistry.titleCase("very very frightening"));
        assertEquals("Sniper Duel", PowerRegistry.titleCase("  sniper   duel "));
    }

    @Test
    void displayNameWorksWithFullAndShortKeys() {
        PowerRegistry registry = new PowerRegistry();
        registry.register("nether/all_effects", "How Did We Get Here?", dummyPower(), "Item Disable: ", HashMap::new);

        assertEquals("How Did We Get Here?", registry.displayName("nether/all_effects"));
        assertEquals("How Did We Get Here?", registry.displayName("all_effects"));
    }

    @Test
    void unknownKeysFallBackToTitleCase() {
        PowerRegistry registry = new PowerRegistry();
        assertEquals("Very Very Frightening", registry.displayName("adventure/very_very_frightening"));
        assertEquals("", registry.displayName(null));
        assertNull(registry.get("adventure/not_a_power"));
        assertNull(registry.get(null));
    }

    @Test
    void registeringTheSameKeyTwiceFails() {
        PowerRegistry registry = new PowerRegistry();
        registry.register("adventure/sniper_duel", "Sniper Duel", dummyPower(), "", HashMap::new);

        assertThrows(IllegalStateException.class,
                () -> registry.register("adventure/sniper_duel", "Again", dummyPower(), "", HashMap::new));
    }

    @Test
    void allKeepsRegistrationOrder() {
        PowerRegistry registry = new PowerRegistry();
        registry.register("b/second", "Second", dummyPower(), "", HashMap::new);
        registry.register("a/first", "First", dummyPower(), "", HashMap::new);

        List<String> keys = registry.all().stream().map(PowerRegistry.PowerDefinition::key).toList();
        assertEquals(List.of("b/second", "a/first"), keys);
    }

    @Test
    void cooldownMapIsReadEachTime() {
        // The supplier is called on every lookup, so a power can replace its map and the bar follows.
        HashMap<UUID, Long> map = new HashMap<>();
        PowerRegistry registry = new PowerRegistry();
        registry.register("a/x", "X", dummyPower(), "", () -> map);

        assertSame(map, registry.get("a/x").primaryCooldowns().get());
    }

    // ---------------------------------------------------------------- SimpleS5 name helpers

    @Test
    void simpleS5NameHelpers() {
        // These helpers use no plugin state, so a mock that runs the real methods is enough.
        SimpleS5 plugin = mock(SimpleS5.class, CALLS_REAL_METHODS);

        assertEquals("Sniper Duel", plugin.getAdvancementNameFormattedFromUnformattedString("adventure/sniper_duel"));
        assertEquals("sniper_duel", plugin.getAdvancementNameUnformattedFromFormattedString("Sniper Duel"));
        assertEquals("Instant Health", plugin.titleCaseString("instant health"));
    }
}
