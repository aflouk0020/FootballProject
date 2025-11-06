package ie.tus.oop1.football.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Team implements FootballEntity.
 * Shows: encapsulation, defensive copying, method overloading, this()/this.
 */
public final class Team implements FootballEntity {
    private Integer id; // nullable until persisted
    private String name;
    private String city;
    private int foundedYear;

    private final List<Player> players = new ArrayList<>();

    public Team(String name, String city, int foundedYear) {
        this(null, name, city, foundedYear);
    }

    public Team(Integer id, String name, String city, int foundedYear) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.foundedYear = foundedYear;
    }

    // Overloading demo: create with name only (sensible defaulting via this())
    public Team(String name) {
        this(name, "Unknown", 1900);
    }

    public Integer getIdBoxed() { return id; }      // for DAOs
    @Override public int id() { return id == null ? 0 : id; }
    @Override public String name() { return name; }

    public String getName() { return name; }
    public String getCity() { return city; }
    public int getFoundedYear() { return foundedYear; }

    public void setId(int id) { this.id = id; }     // set by DAO after insert
    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
    public void setFoundedYear(int foundedYear) { this.foundedYear = foundedYear; }

    public void addPlayer(Player p) {
        if (p != null) players.add(p);
    }

    // Defensive copying + immutability to caller
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(players));
    }

    // varargs convenience
    public void addPlayers(Player... many) {
        if (many != null) {
            for (var p : many) addPlayer(p); // LVTI (var)
        }
    }

    @Override
    public String toString() {
        return name + " (" + city + ", " + foundedYear + ")";
    }
}
