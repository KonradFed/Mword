package com.example.demo.pg;

import java.time.LocalDate;

/**
 * Projekcja interfejsowa — nazwy metod muszą odpowiadać aliasom z SELECT.
 */
public interface PgEmployeeRow {
    Long getEmployeeId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPhone();
    LocalDate getHireDate();
    String getJobId();
    Long getDepartmentId();
}
