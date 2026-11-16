import java.time.LocalDate;
import java.util.*;

public class Booking {
    private int id;
    private LocalDate bookingDate;
    private LocalDate expirationDate;
    private int clientId;
    private int employeeId;
    private int realEstateObjectId;
    private String status;

    public Booking(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.bookingDate = Values.date(row, "booking_date");
        this.expirationDate = Values.date(row, "expiration_date");
        this.clientId = Values.integer(row, "client_id");
        this.employeeId = Values.integer(row, "employee_id");
        this.realEstateObjectId = Values.integer(row, "real_estate_object_id");
        this.status = Values.string(row, "status");
    }
    public int getId() { return id; }
    public LocalDate getBookingDate() { return bookingDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public int getClientId() { return clientId; }
    public int getEmployeeId() { return employeeId; }
    public int getRealEstateObjectId() { return realEstateObjectId; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public boolean overlaps(LocalDate start, LocalDate end) {
        return "active".equals(status) && !bookingDate.isAfter(end) && !start.isAfter(expirationDate);
    }

    public Booking bookObject(AgencyData data) {
        if (data.bookings().stream().anyMatch(existing -> existing.getId() == id))
            throw new IllegalArgumentException("Booking already registered or ID is stale");
        Participant buyer = AgencyData.require(data.clients(), Client::getId, clientId, "Client");
        if (!buyer.canPurchase()) throw new IllegalArgumentException("Participant cannot purchase");
        AgencyData.require(data.employees(), Employee::getId, employeeId, "Employee");
        RealEstateObject object = data.property(realEstateObjectId);
        String reason = data.availability(object, bookingDate, expirationDate, null);
        if (reason != null) throw new IllegalArgumentException(reason);
        data.bookings().add(this);
        object.setRealEstateStatusId(3);
        return this;
    }
}
