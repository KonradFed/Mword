package com.example.demo.pg;

import java.time.LocalDate;

/** Projekcja wiersza z employees + nazwy działu + tytułu stanowiska. */
public interface PgEmployeeRow {
    Long getEmployeeId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPhone();
    LocalDate getHireDate();

    Long getDepartmentId();
    String getDepartmentName();
    String getLocation();      // z tabeli departments

    Long getJobId();
    String getJobTitle();      // z tabeli jobs (alias jobTitle)
}
