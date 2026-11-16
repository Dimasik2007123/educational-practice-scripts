import java.time.LocalDate;
import java.util.Map;

public class RealEstateObject {
    private int id;
    private int realEstateTypeId;
    private int index;
    private String country;
    private String city;
    private String street;
    private int house;
    private int building;
    private String cadastralNumber;
    private int floor;
    private int apartmentNumber;
    private int roomsCount;
    private double totalArea;
    private double livingArea;
    private int constructionStatusId;
    private LocalDate completionDate;
    private long price;
    private int realEstateStatusId;

    public RealEstateObject(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.realEstateTypeId = Values.integer(row, "real_estate_type_id");
        this.index = Values.integer(row, "index");
        this.country = Values.string(row, "country");
        this.city = Values.string(row, "city");
        this.street = Values.string(row, "street");
        this.house = Values.integer(row, "house");
        this.building = Values.integer(row, "building");
        this.cadastralNumber = Values.string(row, "cadastral_number");
        this.floor = Values.integer(row, "floor");
        this.apartmentNumber = Values.integer(row, "apartment_number");
        this.roomsCount = Values.integer(row, "rooms_count");
        this.totalArea = Values.decimal(row, "total_area");
        this.livingArea = Values.decimal(row, "living_area");
        this.constructionStatusId = Values.integer(row, "construction_status_id");
        this.completionDate = Values.date(row, "completion_date");
        this.price = Values.longInteger(row, "price");
        this.realEstateStatusId = Values.integer(row, "real_estate_status_id");
    }
    public int getId() { return id; }
    public int getRealEstateTypeId() { return realEstateTypeId; }
    public int getIndex() { return index; }
    public String getCountry() { return country; }
    public String getCity() { return city; }
    public String getStreet() { return street; }
    public int getHouse() { return house; }
    public int getBuilding() { return building; }
    public String getCadastralNumber() { return cadastralNumber; }
    public int getFloor() { return floor; }
    public int getApartmentNumber() { return apartmentNumber; }
    public int getRoomsCount() { return roomsCount; }
    public double getTotalArea() { return totalArea; }
    public double getLivingArea() { return livingArea; }
    public int getConstructionStatusId() { return constructionStatusId; }
    public LocalDate getCompletionDate() { return completionDate; }
    public long getPrice() { return price; }
    public int getRealEstateStatusId() { return realEstateStatusId; }
    public void setRealEstateStatusId(int value) { this.realEstateStatusId = value; }
    public boolean matches(Map<String,Object> criteria) {
        return equal(criteria, "floor", floor) && equal(criteria, "rooms_count", roomsCount)
            && equal(criteria, "city", city) && min(criteria, "min_price", price) && max(criteria, "max_price", price)
            && min(criteria, "min_area", totalArea) && max(criteria, "max_area", totalArea)
            && equal(criteria, "type_id", realEstateTypeId) && equal(criteria, "real_estate_status_id", realEstateStatusId)
            && equal(criteria, "construction_status_id", constructionStatusId)
            && (criteria.get("min_completion_date") == null || !completionDate.isBefore(LocalDate.parse(criteria.get("min_completion_date").toString())))
            && (criteria.get("max_completion_date") == null || !completionDate.isAfter(LocalDate.parse(criteria.get("max_completion_date").toString())));
    }
    private static boolean equal(Map<String,Object> criteria, String key, Object actual) {
        Object expected = criteria.get(key);
        if (expected == null) return true;
        if (expected instanceof Number && actual instanceof Number) return ((Number) expected).doubleValue() == ((Number) actual).doubleValue();
        return expected.equals(actual);
    }
    private static boolean min(Map<String,Object> criteria, String key, double value) { return criteria.get(key) == null || value >= ((Number)criteria.get(key)).doubleValue(); }
    private static boolean max(Map<String,Object> criteria, String key, double value) { return criteria.get(key) == null || value <= ((Number)criteria.get(key)).doubleValue(); }
}
