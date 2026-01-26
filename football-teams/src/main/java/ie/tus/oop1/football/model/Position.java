package ie.tus.oop1.football.model;

/**
 * Enum with behaviour: description + category flag.
 */

public enum Position {
	
    GOALKEEPER("Responsible for saving goals", false),
    DEFENDER("Protects the defensive line", false),
    MIDFIELDER("Links defence and attack", true),
    FORWARD("Scores goals", true);

    private final String description;
    private final boolean attacking;

    Position(String description, boolean attacking) {
        this.description = description;
        this.attacking = attacking;
    }
    

    public String description() { return description; }
    public boolean isAttacking() { return attacking; }

    public String shortCode() {
        return switch (this) {
            case GOALKEEPER -> "GK";
            case DEFENDER -> "DF";
            case MIDFIELDER -> "MF";
            case FORWARD -> "FW";
        };
    }
}
