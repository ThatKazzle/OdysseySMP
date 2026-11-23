package powers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code HashMap<UUID, Long>} field on a power that stores a time but is not a cooldown
 * (e.g. "ability active until"), so {@link ParentPowerClass#resetCooldowns(java.util.UUID)} leaves it alone.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotACooldown {
}
