package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.*;

public class Sale implements CrudOperations {
    private int id;
    private LocalDateTime saleDate;
    private Employee employee;
    private Client client;
    private ContractType contractType;
    private String notes;
    private List<SaleItem> items = new ArrayList<>();
    private BigDecimal reportTotal = BigDecimal.ZERO;

    public Sale(Employee employee, Client client, ContractType contractType, String notes) {
        this.employee = employee;
        this.client = client;
        this.contractType = contractType;
        this.notes = notes;
        this.saleDate = LocalDateTime.now();
    }

    @Override
    public void add() {
        String sql = "INSERT INTO sale (sale_date, employee_id, client_id, contract_type_id, notes) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(saleDate));
            stmt.setInt(2, employee.getId());
            stmt.setInt(3, client.getId());
            stmt.setInt(4, contractType.getId());
            stmt.setString(5, notes);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) id = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Sale getById(int id) {
        String sql = "SELECT id, sale_date, employee_id, client_id, contract_type_id, notes FROM sale WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Employee e = new Employee(null, null, null, null, null);
                e.setId(rs.getInt("employee_id"));
                Client c = new Client(null, null, null, null, null);
                c.setId(rs.getInt("client_id"));
                ContractType ct = new ContractType(rs.getInt("contract_type_id"));
                Sale s = new Sale(e, c, ct, rs.getString("notes"));
                s.id = rs.getInt("id");
                s.saleDate = rs.getTimestamp("sale_date").toLocalDateTime();
                return s;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void addItem(RealEstateObject object, BigDecimal price) {
        SaleItem item = new SaleItem(this, object, price, items.size() + 1);
        items.add(item);
        item.add();
        object.updateStatus(new RealEstateStatus(4, "Продан", "Объект продан"));
    }

    public BigDecimal calculateTotal() {
        if (items.isEmpty()) {
            return reportTotal;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem item : items) {
            total = total.add(item.getPrice());
        }
        return total;
    }

    public void closeSale() {
        for (SaleItem item : items) {
            item.getRealEstateObject().updateStatus(new RealEstateStatus(4, "Продан", "Объект продан"));
        }
    }

    public static Sale createSale(Employee employee, Client client, ContractType contractType,
                                   List<Object[]> itemData, String notes) {
        Sale sale = new Sale(employee, client, contractType, notes);
        sale.add();
        for (Object[] data : itemData) {
            RealEstateObject obj = (RealEstateObject) data[0];
            BigDecimal price = (BigDecimal) data[1];
            sale.addItem(obj, price);
        }
        return sale;
    }

    public static List<Sale> generateReport(LocalDateTime start, LocalDateTime end) {
        List<Sale> result = new ArrayList<>();
        String sql = "SELECT s.id, s.sale_date, s.employee_id, s.client_id, s.contract_type_id, s.notes, " +
             "COALESCE(SUM(si.price), 0) as total " +
             "FROM sale s LEFT JOIN sale_item si ON s.id = si.sale_id " +
             "WHERE s.sale_date BETWEEN ? AND ? GROUP BY s.id";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Employee e = new Employee(null, null, null, null, null);
                e.setId(rs.getInt("employee_id"));
                Client c = new Client(null, null, null, null, null);
                c.setId(rs.getInt("client_id"));
                ContractType ct = new ContractType(rs.getInt("contract_type_id"));
                Sale s = new Sale(e, c, ct, rs.getString("notes"));
                s.id = rs.getInt("id");
                s.saleDate = rs.getTimestamp("sale_date").toLocalDateTime();
                s.reportTotal = rs.getBigDecimal("total");
                result.add(s);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public ContractType getContractType() { return contractType; }
    public void setContractType(ContractType contractType) { this.contractType = contractType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }

    public static class SaleItemData {
        public RealEstateObject object;
        public BigDecimal price;
        public SaleItemData(RealEstateObject object, BigDecimal price) {
            this.object = object;
            this.price = price;
        }
    }
}
