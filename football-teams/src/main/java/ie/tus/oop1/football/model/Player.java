package ie.tus.oop1.football.model;

public class Player {
    private int id;
    private String name;
    private String position;
    private int age;
    private int teamId; // foreign key reference

    public Player(String name, String position, int age, int teamId) {
        this.name = name;
        this.position = position;
        this.age = age;
        this.teamId = teamId;
    }

    public Player(int id, String name, String position, int age, int teamId) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.age = age;
        this.teamId = teamId;
    }


    public int getId() { return id; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public int getAge() { return age; }
    public int getTeamId() { return teamId; }

    @Override
    public String toString() {
        return String.format("Player{id=%d, name='%s', position='%s', age=%d, teamId=%d}", 
                              id, name, position, age, teamId);
    }
}
