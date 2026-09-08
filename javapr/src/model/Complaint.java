package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.time.LocalDate;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class Complaint implements CrudOperations {
    private int id;
    private LocalDate fillingDate;
    private Sale sale;
    private Client client;
    private RealEstateObject realEstateObject;
    private String status;
    private List<ComplaintItem> items = new ArrayList<>();

    public Complaint(Sale sale, Client client, RealEstateObject realEstateObject, String status) {
        this.sale = sale;
        this.client = client;
        this.realEstateObject = realEstateObject;
        this.status = status;
        this.fillingDate = LocalDate.now();
    }

    @Override
    public void add() {
        String sql = "INSERT INTO complaint (filling_date, sale_id, client_id, real_estate_object_id, status) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(fillingDate));
            stmt.setInt(2, sale.getId());
            stmt.setInt(3, client.getId());
            stmt.setInt(4, realEstateObject.getId());
            stmt.setString(5, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) id = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Complaint getById(int id) {
        String sql = "SELECT id, filling_date, sale_id, client_id, real_estate_object_id, status " +
                     "FROM complaint WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Sale s = new Sale(null, null, null, null);
                s.setId(rs.getInt("sale_id"));
                Client c = new Client(null, null, null, null, null);
                c.setId(rs.getInt("client_id"));
                RealEstateObject obj = new RealEstateObject(null, null, null, 0, 0, null, 0, null, null, null, null, null);
                obj.setId(rs.getInt("real_estate_object_id"));
                Complaint comp = new Complaint(s, c, obj, rs.getString("status"));
                comp.id = rs.getInt("id");
                comp.fillingDate = rs.getDate("filling_date").toLocalDate();
                return comp;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void addItem(String description, Requirement requirement, BigDecimal claimAmount) {
        ComplaintItem item = new ComplaintItem(this, requirement, description, claimAmount);
        items.add(item);
        item.add();
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
        String sql = "UPDATE complaint SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getFillingDate() { return fillingDate; }
    public void setFillingDate(LocalDate fillingDate) { this.fillingDate = fillingDate; }
    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public RealEstateObject getRealEstateObject() { return realEstateObject; }
    public void setRealEstateObject(RealEstateObject realEstateObject) { this.realEstateObject = realEstateObject; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ComplaintItem> getItems() { return items; }
    public void setItems(List<ComplaintItem> items) { this.items = items; }
}
