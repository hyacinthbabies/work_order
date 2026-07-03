package com.njbank.ticket.repository;

import com.njbank.ticket.entity.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findByEnabledTrueOrderBySortOrderAsc();

    List<KnowledgeArticle> findByCategoryAndEnabledTrueOrderBySortOrderAsc(String category);

    @Query("SELECT a FROM KnowledgeArticle a WHERE a.enabled = true AND " +
           "(a.title LIKE %:keyword% OR a.keywords LIKE %:keyword% OR a.tags LIKE %:keyword%) " +
           "ORDER BY a.sortOrder ASC")
    List<KnowledgeArticle> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT a FROM KnowledgeArticle a WHERE a.enabled = true AND " +
           "(a.title LIKE %:keyword% OR a.keywords LIKE %:keyword% OR a.tags LIKE %:keyword% OR a.content LIKE %:keyword%) " +
           "ORDER BY a.sortOrder ASC")
    List<KnowledgeArticle> findRelatedArticles(@Param("keyword") String keyword);
}
