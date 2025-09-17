package com.example.demo.pg;

import java.math.BigDecimal;

public interface PgEmployeeRow {
    Integer getEmployeeId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPhone();
    String getDepartment();
    String getJobTitle();
    BigDecimal getSalary();
}
