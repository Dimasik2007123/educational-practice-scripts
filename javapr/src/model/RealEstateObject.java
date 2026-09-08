package javapr.src.model;

import javapr.src.service.DatabaseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.*;
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
                RealEstateStatus st = new RealEstateStatus(rs.getInt("real_estate_status_id"));
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
                                                        BigDecimal minPrice, BigDecimal maxPrice) {
        return searchObjects(floor, roomsCount, minPrice, maxPrice,
                     null, null, null, null);
        }

        public static List<RealEstateObject> searchObjects(Integer floor, Integer roomsCount,
                                BigDecimal minPrice, BigDecimal maxPrice,
                                String city, Double minArea, Double maxArea,
                                Integer typeId) {
        List<RealEstateObject> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id, real_estate_type_id, (address).*, cadastral_number, floor, apartment_number, " +
            "rooms_count, total_area, living_area, construction_status_id, completion_date, price, " +
            "real_estate_status_id FROM real_estate_object WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (floor != null) { sql.append(" AND floor = ?"); params.add(floor); }
        if (roomsCount != null) { sql.append(" AND rooms_count = ?"); params.add(roomsCount); }
        if (minPrice != null) { sql.append(" AND price >= ?"); params.add(minPrice); }
        if (maxPrice != null) { sql.append(" AND price <= ?"); params.add(maxPrice); }
        if (city != null) { sql.append(" AND (address).city = ?"); params.add(city); }
        if (minArea != null) { sql.append(" AND total_area >= ?"); params.add(minArea); }
        if (maxArea != null) { sql.append(" AND total_area <= ?"); params.add(maxArea); }
        if (typeId != null) { sql.append(" AND real_estate_type_id = ?"); params.add(typeId); }

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RealEstateType type = new RealEstateType(rs.getInt("real_estate_type_id"));
                Address addr = new Address(rs.getInt("index"), rs.getString("country"), rs.getString("city"),
                                           rs.getString("street"), rs.getInt("house"), rs.getInt("building"));
                ConstructionStatus cs = new ConstructionStatus(rs.getInt("construction_status_id"));
                RealEstateStatus st = new RealEstateStatus(rs.getInt("real_estate_status_id"));
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
        return status != null && status.getId() == 2;
    }

    public Booking book(Client client, Employee employee) {
        Booking booking = new Booking(client, employee, this);
        booking.bookObject();
        return booking;
    }

    // Геттеры и сеттеры
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