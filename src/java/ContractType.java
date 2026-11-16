import java.time.LocalDate;
import java.util.Map;

public class ContractType {
    private int id;
    private String name;
    private String description;

    public ContractType(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.name = Values.string(row, "name");
        this.description = Values.string(row, "description");
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
