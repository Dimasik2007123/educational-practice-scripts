package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.sql.*;

public class AcceptanceCertificateItem {
    private AcceptanceCertificate acceptanceCertificate;
    private Keys keys;
    private int lineNumber;

    public AcceptanceCertificateItem(AcceptanceCertificate acceptanceCertificate, Keys keys, int lineNumber) {
        this.acceptanceCertificate = acceptanceCertificate;
        this.keys = keys;
        this.lineNumber = lineNumber;
    }

    public void add() {
        String sql = "INSERT INTO acceptance_certificate_item (acceptance_certificate_id, keys_id, line_number) " +
                     "VALUES (?, ?, ?)";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, acceptanceCertificate.getId());
            stmt.setInt(2, keys.getId());
            stmt.setInt(3, lineNumber);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public AcceptanceCertificate getAcceptanceCertificate() { return acceptanceCertificate; }
    public void setAcceptanceCertificate(AcceptanceCertificate acceptanceCertificate) { this.acceptanceCertificate = acceptanceCertificate; }
    public Keys getKeys() { return keys; }
    public void setKeys(Keys keys) { this.keys = keys; }
    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
}
