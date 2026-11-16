import java.time.LocalDate;
import java.util.*;

public class Keys {
    private int id;
    private int realEstateObjectId;
    private int keySetNumber;
    private int quantity;
    private LocalDate issueDate;
    private int lockerCode;
    private int keysTypeId;

    public Keys(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.realEstateObjectId = Values.integer(row, "real_estate_object_id");
        this.keySetNumber = Values.integer(row, "key_set_number");
        this.quantity = Values.integer(row, "quantity");
        this.issueDate = Values.date(row, "issue_date");
        this.lockerCode = Values.integer(row, "locker_code");
        this.keysTypeId = Values.integer(row, "keys_type_id");
    }
    public int getId() { return id; }
    public int getRealEstateObjectId() { return realEstateObjectId; }
    public int getKeySetNumber() { return keySetNumber; }
    public int getQuantity() { return quantity; }
    public LocalDate getIssueDate() { return issueDate; }
    public int getLockerCode() { return lockerCode; }
    public int getKeysTypeId() { return keysTypeId; }


}
