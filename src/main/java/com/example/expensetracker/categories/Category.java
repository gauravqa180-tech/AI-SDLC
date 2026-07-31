package com.example.expensetracker.categories;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(name = "uk_category_name", columnNames = "name"))
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false)
    private boolean systemDefined = false;
}
