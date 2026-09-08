package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.sql.*;

public class Account {
    private Employee employee;
    private String login;
    private String password;

    public Account(Employee employee, String login, String password) {
        this.employee = employee;
        this.login = login;
        this.password = password;
    }

    public boolean authenticate(String login, String password) {
        return this.login.equals(login) && this.password.equals(password);
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
        String sql = "UPDATE account SET password = ? WHERE employee_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, employee.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static Account getAccountByEmployee(Employee employee) {
        String sql = "SELECT login, password FROM account WHERE employee_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employee.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Account(employee, rs.getString("login"), rs.getString("password"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
