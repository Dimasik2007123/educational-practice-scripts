package javapr.src.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javapr.src.service.DatabaseService;
import java.time.LocalDate;

public class Client extends Person {
    private LocalDate birthDate;
    private String email;
    private Passport passport;

    public Client(FullName fullName, String phone, LocalDate birthDate, String email, Passport passport) {
        super(fullName, phone);
        this.birthDate = birthDate;
        this.email = email;
        this.passport = passport;
    }

    public Client(int id) {
        super(null, null);
        this.id = id;
    }

    @Override
    public void add() {
        String sql = "INSERT INTO client (full_name, birth_date, phone, email, passport) " +
                     "VALUES (ROW(?, ?, ?), ?, ?, ?, ROW(?, ?, ?, ?, ?, ?)) RETURNING id";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, fullName.getFirstName());
            stmt.setString(2, fullName.getLastName());
            stmt.setString(3, fullName.getPatronymic());
            stmt.setDate(4, java.sql.Date.valueOf(birthDate));  // LocalDate → java.sql.Date
            stmt.setString(5, phone);
            stmt.setString(6, email);
            stmt.setInt(7, passport.getSeries());
            stmt.setInt(8, passport.getNumber());
            stmt.setString(9, passport.getIssuedBy());
            stmt.setString(10, passport.getUnitCode());
            stmt.setDate(11, java.sql.Date.valueOf(passport.getIssueDate()));
            stmt.setString(12, passport.getRegistrationAddress());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Client getById(int id) {
        String sql = "SELECT id, (full_name).*, birth_date, phone, email, (passport).* FROM client WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                FullName fn = new FullName(
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("patronymic")
                );
                Passport p = new Passport(
                    rs.getInt("passport_series"),
                    rs.getInt("passport_number"),
                    rs.getString("issued_by"),
                    rs.getString("unit_code"),
                    rs.getDate("issue_date").toLocalDate(),
                    rs.getString("registration_address")
                );
                Client c = new Client(
                    fn,
                    rs.getString("phone"),
                    rs.getDate("birth_date").toLocalDate(),
                    rs.getString("email"),
                    p
                );
                c.id = rs.getInt("id");
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Passport getPassport() { return passport; }
    public void setPassport(Passport passport) { this.passport = passport; }
}
