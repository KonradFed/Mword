package com.example.demo.pg;

import java.time.LocalDate;

/**
 * Projekcja JPA dla wiersza pracownika (PG).
 * UWAGA: nazwy getterów MUSZĄ odpowiadać aliasom w SQL!
 */
public interface PgEmployeeRow {

    Long getEmployeeId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPhone();
    LocalDate getHireDate();

    Long getJobId();
    String getJobTitle();
    Integer getJobMinSalary();   // <-- alias: jobMinSalary
    Integer getJobMaxSalary();   // <-- alias: jobMaxSalary

    Long getDepartmentId();
    String getDepartmentName();
    String getLocation();
}
