package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class AcceptanceCertificate implements CrudOperations {
    private int id;
    private LocalDate issueDate;
    private SaleItem saleItem;
    private List<AcceptanceCertificateItem> items = new ArrayList<>();

    public AcceptanceCertificate(SaleItem saleItem) {
        this.saleItem = saleItem;
        this.issueDate = LocalDate.now();
    }

    @Override
    public void add() {
        String sql = "INSERT INTO acceptance_certificate (issue_date, sale_item_id) VALUES (?, ?) RETURNING id";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(issueDate));
            stmt.setInt(2, saleItem.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) id = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public AcceptanceCertificate getById(int id) {
        String sql = "SELECT id, issue_date, sale_item_id FROM acceptance_certificate WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                SaleItem si = new SaleItem(null, null, null, 0);
                si.setId(rs.getInt("sale_item_id"));
                AcceptanceCertificate cert = new AcceptanceCertificate(si);
                cert.id = rs.getInt("id");
                cert.issueDate = rs.getDate("issue_date").toLocalDate();
                return cert;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void addItem(Keys keys, int lineNumber) {
        AcceptanceCertificateItem item = new AcceptanceCertificateItem(this, keys, lineNumber);
        items.add(item);
        item.add();
    }

    public void generateCertificate() {
        this.add();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public SaleItem getSaleItem() { return saleItem; }
    public void setSaleItem(SaleItem saleItem) { this.saleItem = saleItem; }
    public List<AcceptanceCertificateItem> getItems() { return items; }
    public void setItems(List<AcceptanceCertificateItem> items) { this.items = items; }
}
