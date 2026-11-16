import java.time.LocalDate;
import java.util.Map;

public class ConstructionStatus {
    private int id;
    private String name;
    private int number;
    private boolean isAvailableForSale;

    public ConstructionStatus(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.name = Values.string(row, "name");
        this.number = Values.integer(row, "number");
        this.isAvailableForSale = Values.bool(row, "is_available_for_sale");
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public int getNumber() { return number; }
    public boolean getIsAvailableForSale() { return isAvailableForSale; }
}
