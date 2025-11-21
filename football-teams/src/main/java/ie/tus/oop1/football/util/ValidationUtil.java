package ie.tus.oop1.football.util;

import ie.tus.oop1.football.model.Player;

import java.util.List;
import java.util.function.Predicate;

/** Lambdas & method references helpers. */

public final class ValidationUtil
{
    private ValidationUtil() {}

    public static List<Player> filter(List<Player> source, Predicate<Player> predicate)
    {
        return source.stream().filter(predicate).toList();
    }

    public static boolean isAdult(Player p) 
    { 
    	return p.getAge() >= 18;
    }
    
    public static int parseAge(String s) throws ExceptionHandler 
    {
        int age = Integer.parseInt(s.trim());
        
        if (age < 15 || age > 75)
            throw new ExceptionHandler("Age must be between 15 and 75");
        return age;
    }

    public static int parseYear(String s) throws ExceptionHandler 
    {
        int y = Integer.parseInt(s.trim());
        int max = java.time.Year.now().getValue();
        if (y < 1850 || y > max)
            throw new ExceptionHandler("Invalid year");
        return y;
    }
    



}
