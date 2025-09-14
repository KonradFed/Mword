package com.example.demo.employee;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import com.example.demo.graph.Job;
import com.example.demo.graph.Department;
import com.example.demo.graph.Salary;
import com.example.demo.graph.Attendance;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Integer id;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(length = 100, nullable = false)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private List<Salary> salaries;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private List<Attendance> attendanceRecords;

    // get/set
    public Integer getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getHireDate() { return hireDate; }
    public Job getJob() { return job; }
    public Department getDepartment() { return department; }
    public List<Salary> getSalaries() { return salaries; }
    public List<Attendance> getAttendanceRecords() { return attendanceRecords; }
    
    // Setter methods
    public void setId(Integer id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public void setJob(Job job) { this.job = job; }
    public void setDepartment(Department department) { this.department = department; }
    public void setSalaries(List<Salary> salaries) { this.salaries = salaries; }
    public void setAttendanceRecords(List<Attendance> attendanceRecords) { this.attendanceRecords = attendanceRecords; }
}
