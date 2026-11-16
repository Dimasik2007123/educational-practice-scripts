import java.time.LocalDate;
import java.util.Map;

public abstract class Person implements Participant {
    private int id;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String phone;
    private String email;

    public Person(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.firstName = Values.string(row, "first_name");
        this.lastName = Values.string(row, "last_name");
        this.patronymic = Values.string(row, "patronymic");
        this.phone = Values.string(row, "phone");
        this.email = Values.string(row, "email");
    }
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPatronymic() { return patronymic; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public abstract String role();
    public String describe() { return role() + ": " + lastName + " " + firstName; }
}
