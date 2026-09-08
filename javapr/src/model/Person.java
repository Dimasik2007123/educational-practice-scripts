package javapr.src.model;

public abstract class Person implements CrudOperations {
    protected int id;
    protected FullName fullName;
    protected String phone;

    public Person(FullName fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
    }

    public String getFullName() {
        return fullName.getFullName();
    }

    public int getId() { return id; }
    public FullName getFullNameObj() { return fullName; }
    public String getPhone() { return phone; }
    
    public void setId(int id) { this.id = id; }
    public void setFullName(FullName fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public void add() {
        // Реализуется в наследниках
    }

    @Override
    public Object getById(int id) {
        return null;
    }
}
