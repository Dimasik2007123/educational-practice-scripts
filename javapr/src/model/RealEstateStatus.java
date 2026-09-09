package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RealEstateStatus {
    private int id;
    private String name;
    private String description;

    public RealEstateStatus(int id) {
        this.id = id;
    }

    public RealEstateStatus(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static RealEstateStatus findById(int id) {
        String sql = "SELECT id, name, description FROM real_estate_status WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new RealEstateStatus(rs.getInt("id"), rs.getString("name"),
                                            rs.getString("description"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
