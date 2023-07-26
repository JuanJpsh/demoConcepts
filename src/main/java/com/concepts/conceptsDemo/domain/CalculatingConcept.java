package com.concepts.conceptsDemo.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalculatingConcept {
    private String conceptName;
    private String valueType;
    private String value;
    private Boolean associatesEntity;
    private Boolean hasReport;
    private Integer priority;

    public CalculatingConcept() {
    }
}
