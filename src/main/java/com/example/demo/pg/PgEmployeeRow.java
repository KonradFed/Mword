package com.example.demo.pg;

import java.time.LocalDate;

/** Projekcja wiersza do tabeli PG – bez Job ID, z nazwą działu. */
public interface PgEmployeeRow {
    Long getEmployeeId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPhone();
    LocalDate getHireDate();
    Long getDepartmentId();
    String getDepartmentName(); // <- d.name
}
