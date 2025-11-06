package ie.tus.oop1.football.model;

/**
 * Final subclass permitted by FootballEntity.
 * Not persisted (demo-only), but useful for polymorphism and sealed types.
 */
public final class Coach extends Person implements FootballEntity {
    private final int teamId;

    public Coach(String name, int age, int teamId) {
        super(name, age);
        this.teamId = teamId;
    }

    @Override
    public void introduce() {
        System.out.println("Coach " + name + ", age " + age + ", teamId=" + teamId);
    }

    @Override
    public int id() { return teamId; }

    @Override
    public String name() { return "Coach " + getName(); }
}
