package com.example.demo.graph;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.example.demo.employee.Employee;


@Entity
@Table(name = "salaries")
public class Salary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salary_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    public Integer getId() { return id; }
    public Employee getEmployee() { return employee; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
}
