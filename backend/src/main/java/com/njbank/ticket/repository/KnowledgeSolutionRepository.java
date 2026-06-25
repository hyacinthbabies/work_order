package com.njbank.ticket.repository;

import com.njbank.ticket.entity.KnowledgeSolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeSolutionRepository extends JpaRepository<KnowledgeSolution, Long> {
    Optional<KnowledgeSolution> findByScenario(String scenario);
    List<KnowledgeSolution> findByCategoryOrderBySortOrderAsc(String category);
}
