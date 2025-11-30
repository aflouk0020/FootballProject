package ie.tus.oop1.football.model;

/**
 * Abstract base for people in the system.
 * Shows: inheritance, super(), protected fields, abstract method.
 */
public abstract sealed class Person permits Player, Coach {
    protected final String name;
    protected final int age;

    

    protected Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public final String getName() { return name; }           
    public final int getAge() { return age; }
    

    public abstract void introduce();
}
