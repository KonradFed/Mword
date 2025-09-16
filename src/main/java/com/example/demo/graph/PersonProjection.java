package com.example.demo.graph;

import java.util.List;

public interface PersonProjection {
  Integer getId();
  String getFirstName();
  String getLastName();
  String getEmail();
  List<String> getDepartments(); 
}
