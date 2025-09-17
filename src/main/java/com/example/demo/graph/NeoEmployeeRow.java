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
    Long getMinSalary();
    Long getMaxSalary();

    String getDepartmentName();
    String getLocation();

    Long getAmount();
    LocalDate getFromDate();
}
