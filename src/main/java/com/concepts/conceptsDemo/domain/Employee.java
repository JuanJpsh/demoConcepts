package com.concepts.conceptsDemo.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Employee {
    private Long id;
    private int salary;

    public Employee(Long id, int salary) {
        this.id = id;
        this.salary = salary;
    }
}
