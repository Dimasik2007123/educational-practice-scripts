package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.sql.*;
import java.time.LocalDate;

public class Employee extends Person {
    private String email;
    private Position position;
    private LocalDate hireDate;

    public Employee(FullName fullName, String phone, String email, Position position, LocalDate hireDate) {
        super(fullName, phone);
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;
    }

    public Employee(int id) {
        super(null, null);
        this.id = id;
    }

    @Override
    public void add() {
        String sql = "INSERT INTO employee (full_name, position_id, phone, email, hire_date) " +
                     "VALUES (ROW(?, ?, ?), ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName.getFirstName());
            stmt.setString(2, fullName.getLastName());
            stmt.setString(3, fullName.getPatronymic());
            stmt.setInt(4, position.getId());
            stmt.setString(5, phone);
            stmt.setString(6, email);
            stmt.setDate(7, Date.valueOf(hireDate));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) id = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Employee getById(int id) {
        String sql = "SELECT id, (full_name).*, position_id, phone, email, hire_date FROM employee WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                FullName fn = new FullName(rs.getString("first_name"), rs.getString("last_name"), rs.getString("patronymic"));
                Position pos = new Position(rs.getInt("position_id"));
                Employee e = new Employee(fn, rs.getString("phone"), rs.getString("email"), pos,
                                          rs.getDate("hire_date").toLocalDate());
                e.id = rs.getInt("id");
                return e;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}
