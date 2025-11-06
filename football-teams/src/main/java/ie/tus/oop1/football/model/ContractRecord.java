package ie.tus.oop1.football.model;

/**
 * Java record = built-in immutable value type (call-by-value semantics).
 */
public record ContractRecord(String playerName, double annualSalary, int years) {
    public ContractRecord {
        if (annualSalary < 0 || years <= 0)
            throw new IllegalArgumentException("Invalid contract: " + annualSalary + " x " + years);
    }
    public double totalValue() { return annualSalary * years; }
}
