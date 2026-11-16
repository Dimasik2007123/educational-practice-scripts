import java.time.LocalDate;
import java.util.Map;

public class Employee extends Person {
    private int positionId;
    private LocalDate hireDate;

    public Employee(Map<String, Object> row) {
        super(row);
        this.positionId = Values.integer(row, "position_id");
        this.hireDate = Values.date(row, "hire_date");
    }
    public int getPositionId() { return positionId; }
    public LocalDate getHireDate() { return hireDate; }
    @Override public String role() { return "employee"; }
    @Override public boolean canPurchase() { return false; }
}
