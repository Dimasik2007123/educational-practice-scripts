import java.time.LocalDate;
import java.util.Map;

public class Client extends Person {
    private LocalDate birthDate;
    private int passportSeries;
    private int passportNumber;
    private LocalDate issueDate;
    private String issuedBy;
    private String unitCode;
    private String registrationAddress;

    public Client(Map<String, Object> row) {
        super(row);
        this.birthDate = Values.date(row, "birth_date");
        this.passportSeries = Values.integer(row, "passport_series");
        this.passportNumber = Values.integer(row, "passport_number");
        this.issueDate = Values.date(row, "issue_date");
        this.issuedBy = Values.string(row, "issued_by");
        this.unitCode = Values.string(row, "unit_code");
        this.registrationAddress = Values.string(row, "registration_address");
    }
    public LocalDate getBirthDate() { return birthDate; }
    public int getPassportSeries() { return passportSeries; }
    public int getPassportNumber() { return passportNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public String getIssuedBy() { return issuedBy; }
    public String getUnitCode() { return unitCode; }
    public String getRegistrationAddress() { return registrationAddress; }
    @Override public String role() { return "client"; }
    @Override public boolean canPurchase() { return true; }
}
