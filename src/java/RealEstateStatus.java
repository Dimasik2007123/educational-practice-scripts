import java.time.LocalDate;
import java.util.Map;

public class RealEstateStatus {
    private int id;
    private String name;
    private String description;

    public RealEstateStatus(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.name = Values.string(row, "name");
        this.description = Values.string(row, "description");
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
