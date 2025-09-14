package com.example.demo.graph;
import org.springframework.data.neo4j.core.schema.*;
import java.util.*;

@Node("Person")
public class PersonNode {
  @Id @GeneratedValue private Long id;
  private String firstName;
  private String lastName;

  @Relationship(type="WORKS_IN")
  private Set<Department> worksIn = new HashSet<>();

  public PersonNode() {}
  public PersonNode(String f, String l){ this.firstName=f; this.lastName=l; }
  public void addDepartment(Department d){ worksIn.add(d); }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public Set<Department> getWorksIn() { return worksIn; }
  public void setWorksIn(Set<Department> worksIn) { this.worksIn = worksIn; }
}
