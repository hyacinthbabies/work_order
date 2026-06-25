package com.njbank.ticket.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "knowledge_keywords")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String keywordName;

    @Column(length = 1000)
    private String keywords;

    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
