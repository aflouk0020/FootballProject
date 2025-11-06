package ie.tus.oop1.football.model;

import ie.tus.oop1.football.util.InvalidAgeException;

/**
 * Player extends Person and implements FootballEntity.
 * Mostly immutable, except id assigned by DB. Demonstrates checked exception in factory.
 */
public final class Player extends Person implements FootballEntity {
    private Integer id;               // DB id (nullable until inserted)
    private final Position position;
    private final int teamId;         // foreign key

    // main constructor used by your existing code
    public Player(String name, Position position, int age, int teamId) {
        super(name, age);
        this.position = position;
        this.teamId = teamId;
    }

    // optional: construct with id (e.g., reading from DB)
    public Player(Integer id, String name, Position position, int age, int teamId) {
        this(name, position, age, teamId);
        this.id = id;
    }

    // Factory with validation (checked exception)
    public static Player createValidated(String name, Position position, int age, int teamId)
            throws InvalidAgeException {
        if (age < 16) throw new InvalidAgeException("Player too young: " + age);
        return new Player(name, position, age, teamId);
    }

    public Integer getIdBoxed() { return id; }
    public void setId(int id) { this.id = id; } // DAO sets after insert

    public Position getPosition() { return position; }
    public int getTeamId() { return teamId; }

    @Override
    public void introduce() {
        System.out.println("Hi, I'm " + name + ", a " + position.shortCode() + " (" + position.description() + ").");
    }

    // FootballEntity mapping
    @Override public int id() { return teamId; } // map to team id for demo
    @Override public String name() { return getName(); }

    @Override
    public String toString() {
        return name + " (" + position + ", age " + age + ", teamId " + teamId + ")";
    }
}
