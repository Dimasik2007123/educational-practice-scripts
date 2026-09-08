package javapr.src.model;

public class Position {
    private int id;
    private String name;
    private int salary;

    public Position(int id) {
        this.id = id;
    }

    public Position(String name, int salary) {
        this.name = name;
        this.salary = salary;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSalary() { return salary; }
    public void setSalary(int salary) { this.salary = salary; }
}
