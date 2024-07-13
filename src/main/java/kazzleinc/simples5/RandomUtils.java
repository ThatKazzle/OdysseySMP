package kazzleinc.simples5;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    /**
     * Generates a random integer between min (inclusive) and max (inclusive).
     *
     * @param min The minimum value (inclusive).
     * @param max The maximum value (inclusive).
     * @return A random integer between min and max.
     */
    public static int getRandomIntInRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Generates a random double between min (inclusive) and max (exclusive).
     *
     * @param min The minimum value (inclusive).
     * @param max The maximum value (exclusive).
     * @return A random double between min and max.
     */
    public static double getRandomDoubleInRange(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}