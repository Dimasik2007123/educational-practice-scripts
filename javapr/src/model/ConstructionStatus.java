package javapr.src.model;

public class ConstructionStatus {
    private int id;
    private String name;
    private int number;
    private boolean isAvailableForSale;

    public ConstructionStatus(int id) {
        this.id = id;
    }

    public ConstructionStatus(String name, int number, boolean isAvailableForSale) {
        this.name = name;
        this.number = number;
        this.isAvailableForSale = isAvailableForSale;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public boolean isAvailableForSale() { return isAvailableForSale; }
    public void setAvailableForSale(boolean availableForSale) { isAvailableForSale = availableForSale; }
}
