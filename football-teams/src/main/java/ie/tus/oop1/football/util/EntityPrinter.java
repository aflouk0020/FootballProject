package ie.tus.oop1.football.util;

import ie.tus.oop1.football.model.*;

/**
 * Works on Java 17+ (no preview features required).
 * Uses instanceof pattern matching instead of switch expressions.
 */
public final class EntityPrinter {
    private EntityPrinter() {}

    public static String describe(FootballEntity e) 
    {
        if (e instanceof Player p) 
        {
            return "Player: " + p.getName() + " | Pos=" + p.getPosition();
        } 
        else if (e instanceof Team t) 
        {
            return "Team: " + t.getName() + " from " + t.getCity();
        } 
        else if (e instanceof Coach c) 
        {
            return "Coach: " + c.getName() + " (teamId=" + c.id() + ")";
        } else 
        {
            return "Unknown entity: " + e.getClass().getSimpleName();
        }
    }
    

//    public static String printModernFormat(FootballEntity e) {
//        String name = e.name();
//        int id = e.id();
//
//    
//        return STR."Modern entity: \{name} (#\{id})";
//    }

}