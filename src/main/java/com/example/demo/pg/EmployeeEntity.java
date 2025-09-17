package com.example.demo.pg;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Integer employeeId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "job_id")
    private Integer jobId;

    @Column(name = "department_id")
    private Integer departmentId;

    // --- GETTERS (ważne dla Thymeleaf) ---
    public Integer getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getHireDate() { return hireDate; }
    public Integer getJobId() { return jobId; }
    public Integer getDepartmentId() { return departmentId; }
}
