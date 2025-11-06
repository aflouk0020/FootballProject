package ie.tus.oop1.football.util;

import ie.tus.oop1.football.model.Player;

import java.util.List;
import java.util.function.Predicate;

/** Lambdas & method references helpers. */
public final class ValidationUtil {
    private ValidationUtil() {}

    public static List<Player> filter(List<Player> source, Predicate<Player> predicate) {
        return source.stream().filter(predicate).toList();
    }

    public static boolean isAdult(Player p) { return p.getAge() >= 18; } // method reference target
}
