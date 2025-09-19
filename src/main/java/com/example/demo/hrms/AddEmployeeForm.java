package com.example.demo.hrms;

import java.time.LocalDate;

/** Formularz „Add” – bez employeeId i bez jobId (oba auto). */
public class AddEmployeeForm {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;

    // job_id tworzymy automatycznie, jeśli podasz te pola:
    private String title;     // JOBS.title
    private Long minSalary;   // JOBS.min_salary
    private Long maxSalary;   // JOBS.max_salary

    private Long departmentId;    // PG: employees.department_id
    private String departmentName; // Neo4j fallback, opcjonalne
    private String location;       // Neo4j fallback, opcjonalne

    // Wynagrodzenie (Neo4j)
    private Long amount;       // Neo4j :Wynagrodzenie.kwota
    private LocalDate fromDate;

    // get/set
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getMinSalary() { return minSalary; }
    public void setMinSalary(Long minSalary) { this.minSalary = minSalary; }
    public Long getMaxSalary() { return maxSalary; }
    public void setMaxSalary(Long maxSalary) { this.maxSalary = maxSalary; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
}
