package com.njbank.ticket.repository;

import com.njbank.ticket.entity.KnowledgeKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeKeywordRepository extends JpaRepository<KnowledgeKeyword, Long> {
    List<KnowledgeKeyword> findByCategoryOrderBySortOrderAsc(String category);
    List<KnowledgeKeyword> findByCategory(String category);
}
