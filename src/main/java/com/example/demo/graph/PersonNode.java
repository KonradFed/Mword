package com.example.demo.graph;

import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
@Node("Pracownik")
public class PersonNode {
    @Id
    private Long id;
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;
    
   public Long getEmployeeId() { return employeeId; }
   public String getFirstName() { return firstName; }
   public String getLastName() { return lastName; }
   public String getEmail() { return email; }
   public String getPhone() { return phone; }
   public LocalDate getHireDate() { return hireDate; }
}