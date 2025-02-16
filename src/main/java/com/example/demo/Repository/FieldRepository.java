package com.example.demo.Repository;

import com.example.demo.Model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    boolean existsByName(String name);
}
