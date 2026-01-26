package ie.tus.oop1.football.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom immutable type (not a record) showing defensive copying.
 */
public final class ImmutableSquadSnapshot {
    private final String teamName;
    private final List<String> playerNames; // immu§table snapshot

    public ImmutableSquadSnapshot(String teamName, List<String> names) {
        this.teamName = teamName;
        this.playerNames = Collections.unmodifiableList(new ArrayList<>(names));
    }
    
    public String teamName() { return teamName; }
    public List<String> playerNames() { return playerNames; }
}
