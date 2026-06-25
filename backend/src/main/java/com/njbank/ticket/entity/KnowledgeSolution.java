package com.njbank.ticket.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "knowledge_solutions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String scenario;

    @Column(length = 2000)
    private String solution;

    private String processingTime;

    private String category;

    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
