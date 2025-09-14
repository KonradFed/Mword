package com.example.demo.graph;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.example.demo.employee.Employee;


@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 20, nullable = false)
    private String status;

    public Integer getId() { return id; }
    public Employee getEmployee() { return employee; }
    public LocalDate getDate() { return date; }
    public String getStatus() { return status; }
}
