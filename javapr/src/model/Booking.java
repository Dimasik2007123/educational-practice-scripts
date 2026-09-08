package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.sql.*;
import java.time.LocalDate;

public class Booking implements CrudOperations {
    private int id;
    private LocalDate bookingDate;
    private LocalDate expirationDate;
    private Client client;
    private Employee employee;
    private RealEstateObject realEstateObject;

    public Booking(Client client, Employee employee, RealEstateObject realEstateObject) {
        this.client = client;
        this.employee = employee;
        this.realEstateObject = realEstateObject;
        this.bookingDate = LocalDate.now();
        this.expirationDate = bookingDate.plusDays(14);
    }

    public Booking(LocalDate bookingDate, LocalDate expirationDate,
                   Client client, Employee employee, RealEstateObject realEstateObject) {
        this.bookingDate = bookingDate;
        this.expirationDate = expirationDate;
        this.client = client;
        this.employee = employee;
        this.realEstateObject = realEstateObject;
    }

    @Override
    public void add() {
        String sql = "INSERT INTO booking (booking_date, expiration_date, client_id, employee_id, real_estate_object_id) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(bookingDate));
            stmt.setDate(2, Date.valueOf(expirationDate));
            stmt.setInt(3, client.getId());
            stmt.setInt(4, employee.getId());
            stmt.setInt(5, realEstateObject.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) id = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Booking getById(int id) {
        String sql = "SELECT id, booking_date, expiration_date, client_id, employee_id, real_estate_object_id " +
                     "FROM booking WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Client c = new Client(null, null, null, null, null);
                c.setId(rs.getInt("client_id"));
                Employee e = new Employee(null, null, null, null, null);
                e.setId(rs.getInt("employee_id"));
                RealEstateObject obj = new RealEstateObject(null, null, null, 0, 0, null, 0, null, null, null, null, null);
                obj.setId(rs.getInt("real_estate_object_id"));
                Booking b = new Booking(rs.getDate("booking_date").toLocalDate(),
                                        rs.getDate("expiration_date").toLocalDate(), c, e, obj);
                b.id = rs.getInt("id");
                return b;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void bookObject() {
        if (realEstateObject.isAvailable()) {
            realEstateObject.updateStatus(new RealEstateStatus(3, "В резерве", "Объект забронирован"));
            this.add();
        }
    }

    public void cancelBooking() {
        String sql = "DELETE FROM booking WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            realEstateObject.updateStatus(new RealEstateStatus(2, "Свободно", "Объект доступен"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean isActive() {
        return expirationDate.isAfter(LocalDate.now());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public RealEstateObject getRealEstateObject() { return realEstateObject; }
    public void setRealEstateObject(RealEstateObject realEstateObject) { this.realEstateObject = realEstateObject; }
}
