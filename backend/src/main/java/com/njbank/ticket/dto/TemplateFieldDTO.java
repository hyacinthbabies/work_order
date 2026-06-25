package com.njbank.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateFieldDTO {
    private String fieldName;
    private String placeholder;
    private Boolean required;
}
