package com.example.demo.hrms;

import java.time.LocalDate;

/**
 * DTO do tworzenia pracownika w PG + Neo4j.
 * Uwaga: tytuł (title) do Neo4j bierzemy z wybranego joba; pole title jest tylko fallbackiem
 * jeśli nie wybrano istniejącego joba (wtedy tworzymy nowy job w PG).
 */
public class AddEmployeeForm {

    // --- wspólne ---
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;

    // --- PG ---
    private Long departmentId;   // select z listy działów
    private Integer jobId;       // select z listy jobów (po wybraniu działu)

    // Gdy user nie wybierze istniejącego joba, możemy utworzyć nowy (opcjonalne):
    private String title;        // tytuł stanowiska (fallback)
    private Long minSalary;      // OPCJONALNE – możesz zostawić null
    private Long maxSalary;      // OPCJONALNE – możesz zostawić null

    // --- Neo4j (uzupełniane z wybranego działu + input) ---
    private String departmentName; // nazwa działu (hidden – ustawiana z <select>)
    private String location;       // lokalizacja (input; w Neo nazwą pola jest 'lokalizacja')

    // ===== getters/setters =====
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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getMinSalary() { return minSalary; }
    public void setMinSalary(Long minSalary) { this.minSalary = minSalary; }

    public Long getMaxSalary() { return maxSalary; }
    public void setMaxSalary(Long maxSalary) { this.maxSalary = maxSalary; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
