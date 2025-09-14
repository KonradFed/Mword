package com.example.demo.graph;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import com.example.demo.employee.Employee;


@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Integer id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(name = "min_salary", precision = 10, scale = 2)
    private BigDecimal minSalary;

    @Column(name = "max_salary", precision = 10, scale = 2)
    private BigDecimal maxSalary;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    private List<Employee> employees;

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public BigDecimal getMinSalary() { return minSalary; }
    public BigDecimal getMaxSalary() { return maxSalary; }
    public List<Employee> getEmployees() { return employees; }
}
