package powers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParentPowerClassTest {

    /** A power with two cooldown maps and one map that is not a cooldown. */
    static class SamplePower extends ParentPowerClass {
        final HashMap<UUID, Long> dashCooldowns = new HashMap<>();
        final HashMap<UUID, Long> boltCooldowns = new HashMap<>();
        @NotACooldown
        final HashMap<UUID, Long> stolenUntil = new HashMap<>();
        final HashMap<UUID, Integer> notALongMap = new HashMap<>();

        SamplePower() {
            super(null);
        }

        @Override
        public void action(Player player) {
        }
    }

    private static final String READY = "" + ChatColor.GREEN + ChatColor.BOLD + "Ready!";

    @Test
    void formatCooldownTime() {
        SamplePower power = new SamplePower();

        assertEquals(READY, power.formatCooldownTime(0));
        assertEquals(READY, power.formatCooldownTime(-5));
        assertEquals(ChatColor.RED + "45s", power.formatCooldownTime(45));
        assertEquals(ChatColor.RED + "1m 0s", power.formatCooldownTime(60));
        assertEquals(ChatColor.RED + "2m 5s", power.formatCooldownTime(125));
    }

    @Test
    void setCooldownTakesSeconds() {
        SamplePower power = new SamplePower();
        UUID id = UUID.randomUUID();

        assertFalse(power.isOnCooldown(id, power.dashCooldowns));
        power.setCooldown(id, power.dashCooldowns, 10);
        assertTrue(power.isOnCooldown(id, power.dashCooldowns));

        long endsIn = power.dashCooldowns.get(id) - System.currentTimeMillis();
        assertTrue(endsIn > 9_000 && endsIn <= 10_000, "10 seconds, not 10 ms or 10 minutes: " + endsIn);
    }

    @Test
    void expiredCooldownIsNotOnCooldown() {
        SamplePower power = new SamplePower();
        UUID id = UUID.randomUUID();
        power.dashCooldowns.put(id, System.currentTimeMillis() - 1);

        assertFalse(power.isOnCooldown(id, power.dashCooldowns));
    }

    @Test
    void cooldownTimeLeftShowsReadyWhenNoneIsSet() {
        SamplePower power = new SamplePower();
        assertEquals(READY, power.getCooldownTimeLeft(UUID.randomUUID(), power.dashCooldowns));
    }

    @Test
    void resetCooldownsClearsEveryCooldownMapForThatPlayerOnly() {
        SamplePower power = new SamplePower();
        UUID target = UUID.randomUUID();
        UUID bystander = UUID.randomUUID();

        power.setCooldown(target, power.dashCooldowns, 60);
        power.setCooldown(target, power.boltCooldowns, 60);
        power.setCooldown(bystander, power.dashCooldowns, 60);
        power.stolenUntil.put(target, 123L);
        power.notALongMap.put(target, 7);

        power.resetCooldowns(target);

        assertFalse(power.dashCooldowns.containsKey(target));
        assertFalse(power.boltCooldowns.containsKey(target));
        assertTrue(power.dashCooldowns.containsKey(bystander), "/rc <player> must not touch other players");
        assertEquals(123L, power.stolenUntil.get(target), "@NotACooldown maps are left alone");
        assertEquals(7, power.notALongMap.get(target), "only Map<UUID, Long> fields are cooldowns");
    }
}
