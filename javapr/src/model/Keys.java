package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.sql.*;
import java.time.LocalDate;
import java.util.Random;

public class Keys implements CrudOperations {
    private int id;
    private RealEstateObject realEstateObject;
    private int keySetNumber;
    private int quantity;
    private LocalDate issueDate;
    private Client client;
    private int lockerCode;
    private KeysType keysType;

    public Keys(RealEstateObject realEstateObject, int keySetNumber, int quantity,
                Client client, KeysType keysType) {
        this.realEstateObject = realEstateObject;
        this.keySetNumber = keySetNumber;
        this.quantity = quantity;
        this.client = client;
        this.keysType = keysType;
        this.issueDate = LocalDate.now();
    }

    public int generateLockerCode() {
        Random rand = new Random();
        this.lockerCode = 100000 + rand.nextInt(900000);
        this.add();
        return this.lockerCode;
    }

    @Override
    public void add() {
        String sql = "INSERT INTO keys (real_estate_object_id, key_set_number, quantity, issue_date, " +
                     "client_id, locker_code, keys_type_id) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, realEstateObject.getId());
            stmt.setInt(2, keySetNumber);
            stmt.setInt(3, quantity);
            stmt.setDate(4, Date.valueOf(issueDate));
            stmt.setInt(5, client.getId());
            stmt.setInt(6, lockerCode);
            stmt.setInt(7, keysType.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) id = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Keys getById(int id) {
        String sql = "SELECT id, real_estate_object_id, key_set_number, quantity, issue_date, " +
                     "client_id, locker_code, keys_type_id FROM keys WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                RealEstateObject obj = new RealEstateObject(null, null, null, 0, 0, null, 0, null, null, null, null, null);
                obj.setId(rs.getInt("real_estate_object_id"));
                Client c = new Client(null, null, null, null, null);
                c.setId(rs.getInt("client_id"));
                KeysType kt = new KeysType(rs.getInt("keys_type_id"));
                Keys keys = new Keys(obj, rs.getInt("key_set_number"), rs.getInt("quantity"), c, kt);
                keys.id = rs.getInt("id");
                keys.issueDate = rs.getDate("issue_date").toLocalDate();
                keys.lockerCode = rs.getInt("locker_code");
                return keys;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public RealEstateObject getRealEstateObject() { return realEstateObject; }
    public void setRealEstateObject(RealEstateObject realEstateObject) { this.realEstateObject = realEstateObject; }
    public int getKeySetNumber() { return keySetNumber; }
    public void setKeySetNumber(int keySetNumber) { this.keySetNumber = keySetNumber; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public int getLockerCode() { return lockerCode; }
    public void setLockerCode(int lockerCode) { this.lockerCode = lockerCode; }
    public KeysType getKeysType() { return keysType; }
    public void setKeysType(KeysType keysType) { this.keysType = keysType; }
}
