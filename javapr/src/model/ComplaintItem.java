package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.math.BigDecimal;
import java.sql.*;

public class ComplaintItem {
    private Complaint complaint;
    private Requirement requirement;
    private String description;
    private BigDecimal claimAmount;

    public ComplaintItem(Complaint complaint, Requirement requirement, String description, BigDecimal claimAmount) {
        this.complaint = complaint;
        this.requirement = requirement;
        this.description = description;
        this.claimAmount = claimAmount;
    }

    public void add() {
        String sql = "INSERT INTO complaint_item (complaint_id, requirement_id, description, claim_amount) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, complaint.getId());
            stmt.setInt(2, requirement.getId());
            stmt.setString(3, description);
            stmt.setBigDecimal(4, claimAmount);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }
    public Requirement getRequirement() { return requirement; }
    public void setRequirement(Requirement requirement) { this.requirement = requirement; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getClaimAmount() { return claimAmount; }
    public void setClaimAmount(BigDecimal claimAmount) { this.claimAmount = claimAmount; }
}