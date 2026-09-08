package javapr.src.model;

public class Address {
    private int index;
    private String country;
    private String city;
    private String street;
    private int house;
    private Integer building;

    public Address(int index, String country, String city, String street, int house, Integer building) {
        this.index = index;
        this.country = country;
        this.city = city;
        this.street = street;
        this.house = house;
        this.building = building;
    }

    public String getFullAddress() {
        String addr = index + ", " + country + ", " + city + ", " + street + ", " + house;
        if (building != null) addr += ", стр. " + building;
        return addr;
    }

    public boolean isValid() {
        return index > 0 && country != null && !country.isEmpty() && city != null && !city.isEmpty()
                && street != null && !street.isEmpty() && house > 0;
    }

    public int getIndex() { return index; }
    public String getCountry() { return country; }
    public String getCity() { return city; }
    public String getStreet() { return street; }
    public int getHouse() { return house; }
    public Integer getBuilding() { return building; }
    
    public void setIndex(int index) { this.index = index; }
    public void setCountry(String country) { this.country = country; }
    public void setCity(String city) { this.city = city; }
    public void setStreet(String street) { this.street = street; }
    public void setHouse(int house) { this.house = house; }
    public void setBuilding(Integer building) { this.building = building; }
}
