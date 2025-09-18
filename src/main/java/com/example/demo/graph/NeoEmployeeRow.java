package com.example.demo.graph;

import java.time.LocalDate;

public interface NeoEmployeeRow {
    Long getEmployeeId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPhone();
    LocalDate getHireDate();

    String getTitle();
    Integer getMinSalary();
    Integer getMaxSalary();

    String getDepartmentName();
    String getLocation();

    Integer getAmount();
    LocalDate getFromDate();
}
