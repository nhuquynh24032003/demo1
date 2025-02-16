package com.example.demo.Repository;

import com.example.demo.Model.IssuingAgencies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssuingAgenciesRepository extends JpaRepository<IssuingAgencies, Long> {
    boolean existsByName(String name);
}
