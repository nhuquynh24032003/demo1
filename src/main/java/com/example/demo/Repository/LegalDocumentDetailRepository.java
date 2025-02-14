package com.example.demo.Repository;

import com.example.demo.Model.LegalDocumentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalDocumentDetailRepository extends JpaRepository<LegalDocumentDetail, Long> {
    boolean existsByDetailUrl(String detailUrl);
}
