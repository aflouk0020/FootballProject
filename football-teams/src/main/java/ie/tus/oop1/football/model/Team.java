package ie.tus.oop1.football.model;

import java.util.ArrayList;
import java.util.List;

public final class Team implements FootballEntity {
    private final int id;
    private final String name;
    private final String city;
    private final int foundedYear;
    private final List<Player> players;
    private Coach coach; // 🔹 NEW: 1-to-1 relationship

    public Team(int id, String name, String city, int foundedYear) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.foundedYear = foundedYear;
        this.players = new ArrayList<>();
    }

    public Team(String name, String city, int foundedYear) {
        this(0, name, city, foundedYear);
    }

    public void addPlayer(Player p) { players.add(p); }

    public int id() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public int getFoundedYear() { return foundedYear; }
    public List<Player> getPlayers() { return players; }
    
    
    public String detailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team: ").append(name)
          .append(" from ").append(city)
          .append(" (Founded ").append(foundedYear).append(")\n");
        
        sb.append("Players:\n");
        for (Player p : players) {
            sb.append(" - ").append(p.getName()).append(" (")
              .append(p.getPosition().shortCode()).append(")\n");
        }
        return sb.toString();
    }

  public void addPlayers(Player... many) 
  {
      if (many != null) 
      {
          for (var p : many) addPlayer(p); // LVTI (var)
      }
  }

    // 🔹 1-to-1 coach link
    public Coach getCoach() { return coach; }
    public void setCoach(Coach coach) { this.coach = coach; }

    @Override
    public String name() { return name; }

    @Override
    public String toString() {
        return "%s (%s, %d)".formatted(name, city, foundedYear);
    }
}


//package ie.tus.oop1.football.model;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
///**
// * Team implements FootballEntity.
// * Shows: encapsulation, defensive copying, method overloading, this()/this.
// */
//public final class Team implements FootballEntity {
//    private Integer id; // nullable until persisted
//    private String name;
//    private String city;
//    private int foundedYear;
//
//    private final List<Player> players = new ArrayList<>();
//
//    public Team(String name, String city, int foundedYear)
//    {
//        this(null, name, city, foundedYear);
//    }
//
//    public Team(Integer id, String name, String city, int foundedYear) 
//    {
//        this.id = id;
//        this.name = name;
//        this.city = city;
//        this.foundedYear = foundedYear;
//    }
//
//    // Overloading demo: create with name only (sensible defaulting via this())
//    public Team(String name) {
//        this(name, "Unknown", 1900);
//    }
//
//    public Integer getIdBoxed() { return id; }      // for DAOs
//    @Override public int id() { return id == null ? 0 : id; }
//    @Override public String name() { return name; }
//
//    public String getName()
//    { return name; 
//    }
//    public String getCity() 
//    { 
//    	return city; 
//    }
//    public int getFoundedYear()
//    { 
//    	return foundedYear; 
//    }
//
//    public void setId(int id)
//    { 
//    	this.id = id;
//    }     // set by DAO after insert
//    public void setName(String name) 
//    { 
//    	this.name = name; 
//    }
//    public void setCity(String city) 
//    { 
//    	this.city = city;
//    }
//    public void setFoundedYear(int foundedYear)
//    { 
//    	this.foundedYear = foundedYear; 
//    }
//
//    public void addPlayer(Player p) 
//    {
//        if (p != null) players.add(p);
//    }
//
//    // Defensive copying + immutability to caller
//    public List<Player> getPlayers() 
//    {
//        return Collections.unmodifiableList(new ArrayList<>(players));
//    }
//
//    // varargs convenience
//    public void addPlayers(Player... many) 
//    {
//        if (many != null) 
//        {
//            for (var p : many) addPlayer(p); // LVTI (var)
//        }
//    }
//
//    @Override
//    public String toString() 
//    {
//        return name + " (" + city + ", " + foundedYear + ")";
//    }
//}
