package ie.tus.oop1.football.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a football team in the system.
 * 
 * Each Team can have multiple Players (one-to-many relationship).
 * This class follows encapsulation and includes convenience methods
 * for managing the team's player list.
 * 
 * @author Taha
 */
public class Team {

    private int id;
    private String name;
    private String city;
    private int foundedYear;
    private List<Player> players; 

    /**
     * Constructor used for inserting a new team (no ID yet).
     */
    public Team(String name, String city, int foundedYear) {
        this.name = name;
        this.city = city;
        this.foundedYear = foundedYear;
        this.players = new ArrayList<>();
    }

    /**
     * Constructor used when reading from the database.
     */
    public Team(int id, String name, String city, int foundedYear) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.foundedYear = foundedYear;
        this.players = new ArrayList<>();
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public int getFoundedYear() 
    { 
    	return foundedYear;
    }

    public void setFoundedYear(int foundedYear) 
    { 
    	this.foundedYear = foundedYear;
    }

    public List<Player> getPlayers() 
    {
    	return players;
    }

    public void setPlayers(List<Player> players) 
    { 
    	this.players = players;
    }


    /**
     * Adds a player to this team's list.
     */
    public void addPlayer(Player player) {
        if (players == null) players = new ArrayList<>();
        players.add(player);
    }

    /**
     * Removes a player from this team's list by ID.
     */
    public void removePlayerById(int playerId) {
        if (players != null)
            players.removeIf(p -> p.getId() == playerId);
    }

    // useful for Sets or comparisons
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team)) return false;
        Team team = (Team) o;
        return Objects.equals(name.toLowerCase(), team.name.toLowerCase()) &&
               Objects.equals(city.toLowerCase(), team.city.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), city.toLowerCase());
    }


    @Override
    public String toString() {
        return String.format(
            "Team{id=%d, name='%s', city='%s', foundedYear=%d, players=%d}",
            id, name, city, foundedYear,
            (players == null ? 0 : players.size())
        );
    }
}
