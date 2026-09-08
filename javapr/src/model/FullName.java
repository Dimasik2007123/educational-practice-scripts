package javapr.src.model;

public class FullName {
    private String firstName;
    private String lastName;
    private String patronymic;

    public FullName(String firstName, String lastName, String patronymic) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
    }

    public String getFullName() {
        if (patronymic != null && !patronymic.isEmpty()) {
            return lastName + " " + firstName + " " + patronymic;
        }
        return lastName + " " + firstName;
    }

    public String getShortName() {
        String fi = (firstName != null && !firstName.isEmpty()) ? firstName.charAt(0) + "." : "";
        String pi = (patronymic != null && !patronymic.isEmpty()) ? patronymic.charAt(0) + "." : "";
        return lastName + " " + fi + pi;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPatronymic() { return patronymic; }
    
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }
}