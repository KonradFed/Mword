package com.example.demo.graph;

import org.springframework.data.neo4j.core.schema.*;

@Node("Pracownik")
public class PersonNode {

  @Id
  @Property("employee_id")
  private Integer id;

  @Property("imię")
  private String firstName;

  @Property("nazwisko")
  private String lastName;

  @Property("email")
  private String email;

  public PersonNode() { }

  public PersonNode(Integer id, String firstName, String lastName, String email) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
}
