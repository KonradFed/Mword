package com.example.demo.graph;

import org.springframework.data.neo4j.core.schema.*;

@Node("Dział")
public class DepartmentNode {

  @Id
  @Property("department_id")
  private Integer id;

  @Property("nazwa")
  private String name;

  @Property("lokalizacja")
  private String location;

  public DepartmentNode() { }

  public DepartmentNode(Integer id, String name, String location) {
    this.id = id;
    this.name = name;
    this.location = location;
  }

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getLocation() { return location; }
  public void setLocation(String location) { this.location = location; }
}
