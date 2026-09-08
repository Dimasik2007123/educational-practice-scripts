package javapr.src.model;

import java.time.LocalDate;

public class Passport {
    private int series;
    private int number;
    private String issuedBy;
    private String unitCode;
    private LocalDate issueDate;
    private String registrationAddress;

    public Passport(int series, int number, String issuedBy, String unitCode, 
                    LocalDate issueDate, String registrationAddress) {
        this.series = series;
        this.number = number;
        this.issuedBy = issuedBy;
        this.unitCode = unitCode;
        this.issueDate = issueDate;
        this.registrationAddress = registrationAddress;
    }

    public String getFullPassportInfo() {
        return series + " " + number + ", выдан: " + issuedBy;
    }

    public boolean isValid() {
        return series > 0 && number > 0 && issuedBy != null && !issuedBy.isEmpty();
    }

    public int getSeries() { return series; }
    public int getNumber() { return number; }
    public String getIssuedBy() { return issuedBy; }
    public String getUnitCode() { return unitCode; }
    public LocalDate getIssueDate() { return issueDate; }
    public String getRegistrationAddress() { return registrationAddress; }
    
    public void setSeries(int series) { this.series = series; }
    public void setNumber(int number) { this.number = number; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public void setRegistrationAddress(String registrationAddress) { this.registrationAddress = registrationAddress; }
}