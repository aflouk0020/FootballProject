package ie.tus.oop1.football.model;

/**
 * Sealed interface to constrain which domain types are valid football entities.
 * Demonstrates sealed interfaces, default and static interface methods.
 */
public sealed interface FootballEntity permits Team, Player, Coach {

    int id();
    String name();

    default String entityInfo() {
        return "Entity: " + name() + " [#" + id() + "]";
    }

    // single static helper
    static void printType(FootballEntity e) {
        System.out.println("Entity type: " + e.getClass().getSimpleName());
    }
}
