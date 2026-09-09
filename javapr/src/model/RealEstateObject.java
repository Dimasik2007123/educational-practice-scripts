package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class RealEstateObject implements CrudOperations {
    private int id;
    private RealEstateType type;
    private Address address;
    private String cadastralNumber;
    private int floor;
    private int apartmentNumber;
    private Integer roomsCount;
    private double totalArea;
    private Double livingArea;
    private ConstructionStatus constructionStatus;
    private LocalDate completionDate;
    private BigDecimal price;
    private RealEstateStatus status;

    public RealEstateObject(RealEstateType type, Address address, String cadastralNumber,
                            int floor, int apartmentNumber, Integer roomsCount,
                            double totalArea, Double livingArea, ConstructionStatus constructionStatus,
                            LocalDate completionDate, BigDecimal price, RealEstateStatus status) {
        this.type = type;
        this.address = address;
        this.cadastralNumber = cadastralNumber;
        this.floor = floor;
        this.apartmentNumber = apartmentNumber;
        this.roomsCount = roomsCount;
        this.totalArea = totalArea;
        this.livingArea = livingArea;
        this.constructionStatus = constructionStatus;
        this.completionDate = completionDate;
        this.price = price;
        this.status = status;
    }

    public RealEstateObject(int id) {
        this.id = id;
    }

    @Override
    public void add() {
        String sql = "INSERT INTO real_estate_object (real_estate_type_id, address, cadastral_number, floor, " +
                     "apartment_number, rooms_count, total_area, living_area, construction_status_id, " +
                     "completion_date, price, real_estate_status_id) " +
                     "VALUES (?, ROW(?, ?, ?, ?, ?, ?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, type.getId());
            stmt.setInt(2, address.getIndex());
            stmt.setString(3, address.getCountry());
            stmt.setString(4, address.getCity());
            stmt.setString(5, address.getStreet());
            stmt.setInt(6, address.getHouse());
            stmt.setObject(7, address.getBuilding());
            stmt.setString(8, cadastralNumber);
            stmt.setInt(9, floor);
            stmt.setInt(10, apartmentNumber);
            stmt.setObject(11, roomsCount);
            stmt.setDouble(12, totalArea);
            stmt.setObject(13, livingArea);
            stmt.setInt(14, constructionStatus.getId());
            stmt.setDate(15, java.sql.Date.valueOf(completionDate));
            stmt.setBigDecimal(16, price);
            stmt.setInt(17, status.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) id = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public RealEstateObject getById(int id) {
        String sql = "SELECT id, real_estate_type_id, (address).*, cadastral_number, floor, apartment_number, " +
                     "rooms_count, total_area, living_area, construction_status_id, completion_date, price, " +
                     "real_estate_status_id FROM real_estate_object WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                RealEstateType type = new RealEstateType(rs.getInt("real_estate_type_id"));
                Address addr = new Address(rs.getInt("index"), rs.getString("country"), rs.getString("city"),
                                           rs.getString("street"), rs.getInt("house"), rs.getObject("building") != null ? rs.getInt("building") : null);
                ConstructionStatus cs = new ConstructionStatus(rs.getInt("construction_status_id"));
                RealEstateStatus st = RealEstateStatus.findById(rs.getInt("real_estate_status_id"));
                RealEstateObject obj = new RealEstateObject(
                    type, addr, rs.getString("cadastral_number"),
                    rs.getInt("floor"), rs.getInt("apartment_number"),
                    rs.getObject("rooms_count") != null ? rs.getInt("rooms_count") : null,
                    rs.getDouble("total_area"),
                    rs.getObject("living_area") != null ? rs.getDouble("living_area") : null,
                    cs, rs.getDate("completion_date").toLocalDate(),
                    rs.getBigDecimal("price"), st
                );
                obj.id = rs.getInt("id");
                return obj;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updateStatus(RealEstateStatus newStatus) {
        this.status = newStatus;
        String sql = "UPDATE real_estate_object SET real_estate_status_id = ? WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newStatus.getId());
            stmt.setInt(2, this.id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<RealEstateObject> searchObjects(Integer floor, Integer roomsCount,
                                BigDecimal minPrice, BigDecimal maxPrice,
                                String city, Double minArea, Double maxArea,
                                Integer typeId, Integer statusId,
                                Integer constructionStatusId, boolean availableOnly) {
        List<RealEstateObject> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT reo.id, reo.real_estate_type_id, (reo.address).*, reo.cadastral_number, " +
            "reo.floor, reo.apartment_number, reo.rooms_count, reo.total_area, reo.living_area, " +
            "reo.construction_status_id, reo.completion_date, reo.price, reo.real_estate_status_id, " +
            "ret.name AS real_estate_type_name, ret.description AS real_estate_type_description, " +
            "res.name AS real_estate_status_name, res.description AS real_estate_status_description, " +
            "cs.name AS construction_status_name, cs.number AS construction_status_number, " +
            "cs.is_available_for_sale AS construction_status_available " +
            "FROM real_estate_object reo " +
            "JOIN real_estate_type ret ON ret.id = reo.real_estate_type_id " +
            "JOIN real_estate_status res ON res.id = reo.real_estate_status_id " +
            "JOIN construction_status cs ON cs.id = reo.construction_status_id " +
            "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (floor != null) { sql.append(" AND reo.floor = ?"); params.add(floor); }
        if (roomsCount != null) { sql.append(" AND reo.rooms_count = ?"); params.add(roomsCount); }
        if (minPrice != null) { sql.append(" AND reo.price >= ?"); params.add(minPrice); }
        if (maxPrice != null) { sql.append(" AND reo.price <= ?"); params.add(maxPrice); }
        if (city != null) { sql.append(" AND (reo.address).city = ?"); params.add(city); }
        if (minArea != null) { sql.append(" AND reo.total_area >= ?"); params.add(minArea); }
        if (maxArea != null) { sql.append(" AND reo.total_area <= ?"); params.add(maxArea); }
        if (typeId != null) { sql.append(" AND reo.real_estate_type_id = ?"); params.add(typeId); }
        if (statusId != null) { sql.append(" AND reo.real_estate_status_id = ?"); params.add(statusId); }
        if (constructionStatusId != null) { sql.append(" AND reo.construction_status_id = ?"); params.add(constructionStatusId); }
        if (availableOnly) { sql.append(" AND reo.real_estate_status_id = 2"); }

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RealEstateType type = new RealEstateType(rs.getString("real_estate_type_name"),
                                                         rs.getString("real_estate_type_description"));
                type.setId(rs.getInt("real_estate_type_id"));
                Address addr = new Address(rs.getInt("index"), rs.getString("country"), rs.getString("city"),
                                           rs.getString("street"), rs.getInt("house"),
                                           rs.getObject("building") != null ? rs.getInt("building") : null);
                ConstructionStatus cs = new ConstructionStatus(
                    rs.getString("construction_status_name"),
                    rs.getInt("construction_status_number"),
                    rs.getBoolean("construction_status_available"));
                cs.setId(rs.getInt("construction_status_id"));
                RealEstateStatus st = new RealEstateStatus(
                    rs.getInt("real_estate_status_id"),
                    rs.getString("real_estate_status_name"),
                    rs.getString("real_estate_status_description"));
                RealEstateObject obj = new RealEstateObject(
                    type, addr, rs.getString("cadastral_number"),
                    rs.getInt("floor"), rs.getInt("apartment_number"),
                    rs.getObject("rooms_count") != null ? rs.getInt("rooms_count") : null,
                    rs.getDouble("total_area"),
                    rs.getObject("living_area") != null ? rs.getDouble("living_area") : null,
                    cs, rs.getDate("completion_date").toLocalDate(),
                    rs.getBigDecimal("price"), st
                );
                obj.id = rs.getInt("id");
                result.add(obj);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public boolean isAvailable() {
        try (Connection conn = DatabaseService.getConnection()) {
            conn.setAutoCommit(false);
            boolean available = lockAndReleaseExpiredBooking(conn, id);
            conn.commit();
            if (available) status = RealEstateStatus.findById(2);
            return available;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean lockAndReleaseExpiredBooking(Connection conn, int objectId) throws SQLException {
        String sql = "SELECT reo.real_estate_status_id, b.id, b.expiration_date " +
                     "FROM real_estate_object reo " +
                     "LEFT JOIN booking b ON b.real_estate_object_id = reo.id " +
                     "WHERE reo.id = ? FOR UPDATE OF reo";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, objectId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return false;

            int statusId = rs.getInt(1);
            int bookingId = rs.getInt(2);
            java.sql.Date expirationDate = rs.getDate(3);
            if (statusId == 3 && !rs.wasNull() && bookingId != 0 && expirationDate != null
                    && expirationDate.toLocalDate().isBefore(LocalDate.now())) {
                try (PreparedStatement delete = conn.prepareStatement("DELETE FROM booking WHERE id = ?");
                     PreparedStatement update = conn.prepareStatement(
                             "UPDATE real_estate_object SET real_estate_status_id = 2 WHERE id = ?")) {
                    delete.setInt(1, bookingId);
                    delete.executeUpdate();
                    update.setInt(1, objectId);
                    update.executeUpdate();
                }
                statusId = 2;
            }
            return statusId == 2;
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public RealEstateType getType() { return type; }
    public void setType(RealEstateType type) { this.type = type; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public String getCadastralNumber() { return cadastralNumber; }
    public void setCadastralNumber(String cadastralNumber) { this.cadastralNumber = cadastralNumber; }
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public int getApartmentNumber() { return apartmentNumber; }
    public void setApartmentNumber(int apartmentNumber) { this.apartmentNumber = apartmentNumber; }
    public Integer getRoomsCount() { return roomsCount; }
    public void setRoomsCount(Integer roomsCount) { this.roomsCount = roomsCount; }
    public double getTotalArea() { return totalArea; }
    public void setTotalArea(double totalArea) { this.totalArea = totalArea; }
    public Double getLivingArea() { return livingArea; }
    public void setLivingArea(Double livingArea) { this.livingArea = livingArea; }
    public ConstructionStatus getConstructionStatus() { return constructionStatus; }
    public void setConstructionStatus(ConstructionStatus constructionStatus) { this.constructionStatus = constructionStatus; }
    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public RealEstateStatus getStatus() { return status; }
    public void setStatus(RealEstateStatus status) { this.status = status; }
}