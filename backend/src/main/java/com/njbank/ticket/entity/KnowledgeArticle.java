package com.njbank.ticket.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_article")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 200)
    private String summary;

    @Column(length = 100)
    private String category;

    @Column(length = 500)
    private String tags;

    @Column(length = 100)
    private String author;

    @Column(length = 100)
    private String source;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 1;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
