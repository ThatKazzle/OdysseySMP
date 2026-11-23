package kazzleinc.simples5;

import PowerClasses.ParentPowerClass;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Supplier;

/**
 * The one list of powers. Adding a power means writing its class and adding one
 * {@link #register} line in {@link SimpleS5#registerPowers()}; nothing else needs editing.
 */
public final class PowerRegistry {

    /**
     * @param key              stored power key, e.g. "adventure/sniper_duel" (the advancement it comes from)
     * @param displayName      name shown to players, e.g. "Sniper Duel"
     * @param power            the class that runs the power
     * @param cooldownLabel    label in front of the action-bar cooldown, e.g. "Sniper Vision: "
     * @param primaryCooldowns the cooldown map shown on the action bar
     */
    public record PowerDefinition(String key, String displayName, ParentPowerClass power,
                                  String cooldownLabel, Supplier<HashMap<UUID, Long>> primaryCooldowns) {

        /** "adventure/sniper_duel" -> "sniper_duel" (the key used under config.yml defaults/descriptions). */
        public String shortKey() {
            return PowerRegistry.shortKey(key);
        }

        public String cooldownString(Player player) {
            return power.getCooldownString(player, primaryCooldowns.get(), cooldownLabel);
        }
    }

    private final Map<String, PowerDefinition> byKey = new LinkedHashMap<>();
    private final Map<String, PowerDefinition> byShortKey = new HashMap<>();

    public void register(String key, String displayName, ParentPowerClass power,
                         String cooldownLabel, Supplier<HashMap<UUID, Long>> primaryCooldowns) {
        PowerDefinition def = new PowerDefinition(key, displayName, power, cooldownLabel, primaryCooldowns);
        if (byKey.putIfAbsent(key, def) != null) {
            throw new IllegalStateException("Power registered twice: " + key);
        }
        byShortKey.put(def.shortKey(), def);
    }

    /** Look up by full key ("adventure/sniper_duel"). Null if unknown. */
    public PowerDefinition get(String key) {
        return key == null ? null : byKey.get(key);
    }

    public Collection<PowerDefinition> all() {
        return Collections.unmodifiableCollection(byKey.values());
    }

    /**
     * Display name for a full key ("nether/all_effects") or short key ("all_effects"). Unknown keys
     * fall back to title case ("very_very_frightening" -> "Very Very Frightening"), as before.
     */
    public String displayName(String anyKey) {
        if (anyKey == null) return "";
        PowerDefinition def = byKey.containsKey(anyKey) ? byKey.get(anyKey) : byShortKey.get(shortKey(anyKey));
        return def != null ? def.displayName() : titleCase(shortKey(anyKey).replace('_', ' '));
    }

    static String shortKey(String key) {
        int slash = key.lastIndexOf('/');
        return slash < 0 ? key : key.substring(slash + 1);
    }

    static String titleCase(String input) {
        StringBuilder out = new StringBuilder();
        for (String word : input.split("\\s+")) {
            if (word.isEmpty()) continue;
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
        }
        return out.toString().trim();
    }
}
