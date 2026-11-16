import java.time.LocalDate;
import java.util.Map;

public class Position {
    private int id;
    private String name;
    private long salary;

    public Position(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.name = Values.string(row, "name");
        this.salary = Values.longInteger(row, "salary");
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public long getSalary() { return salary; }
}
