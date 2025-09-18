package com.example.demo.graph;

public interface NeoEmployeeRow {
    Long getEmployeeId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPhone();
    String getHireDate();

    String getTitle();
    Long getMinSalary();
    Long getMaxSalary();

    String getDepartmentName();
    String getLocation();

    Long getAmount();
    String getFromDate();
}
