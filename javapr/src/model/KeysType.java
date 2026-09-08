package javapr.src.model;

public class KeysType {
    private int id;
    private String name;
    private String description;

    public KeysType(int id) {
        this.id = id;
    }

    public KeysType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
