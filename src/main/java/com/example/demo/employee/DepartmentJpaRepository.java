package com.example.demo.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentJpaRepository extends JpaRepository<Department, Integer> {
  Optional<Department> findByName(String name);
}
