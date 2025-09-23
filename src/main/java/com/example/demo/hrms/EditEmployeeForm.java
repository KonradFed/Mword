package com.example.demo.hrms;

import java.time.LocalDate;

/**
 * DTO do edycji pracownika (PG + Neo4j).
 * W PG aktualizujemy dane osobowe + department, a jobId ustawiamy osobno gdy podany.
 * W Neo4j ustawiamy tytuł (z joba) oraz nazwę działu/lokalizację.
 */
public class EditEmployeeForm {

    private Long employeeId;

    // --- wspólne ---
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;

    // --- PG ---
    private Long departmentId;
    private Integer jobId; // może być null – wtedy job nie zmienia się

    // --- Neo4j (hidden + input) ---
    private String departmentName; // z nazwy wybranego działu
    private String location;       // input

    // Dla Neo4j przekazujemy tytuł stanowiska – ustawiany z wybranego joba
    private String title;

    // ===== getters/setters =====
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

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

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Integer getJobId() { return jobId; }
    public void setJobId(Integer jobId) { this.jobId = jobId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
