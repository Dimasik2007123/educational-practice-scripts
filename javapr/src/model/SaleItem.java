package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.math.BigDecimal;
import java.sql.*;

public class SaleItem {
    private int id;
    private Sale sale;
    private RealEstateObject realEstateObject;
    private BigDecimal price;
    private int lineNumber;

    public SaleItem(Sale sale, RealEstateObject realEstateObject, BigDecimal price, int lineNumber) {
        this.sale = sale;
        this.realEstateObject = realEstateObject;
        this.price = price;
        this.lineNumber = lineNumber;
    }

    public void add() {
        String sql = "INSERT INTO sale_item (sale_id, real_estate_object_id, price, line_number) " +
                     "VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sale.getId());
            stmt.setInt(2, realEstateObject.getId());
            stmt.setBigDecimal(3, price);
            stmt.setInt(4, lineNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) id = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    void add(Connection conn) throws SQLException {
        String sql = "INSERT INTO sale_item (sale_id, real_estate_object_id, price, line_number) " +
                     "VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sale.getId());
            stmt.setInt(2, realEstateObject.getId());
            stmt.setBigDecimal(3, price);
            stmt.setInt(4, lineNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) id = rs.getInt(1);
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }
    public RealEstateObject getRealEstateObject() { return realEstateObject; }
    public void setRealEstateObject(RealEstateObject realEstateObject) { this.realEstateObject = realEstateObject; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
}
