package com.example.expensetracker.config;

import com.example.expensetracker.category.Category;
import com.example.expensetracker.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<String> defaults = List.of(
                "Food",
                "Transport",
                "Rent",
                "Utilities",
                "Entertainment",
                "Health",
                "Shopping",
                "Other"
        );

        for (String name : defaults) {
            categoryRepository.save(Category.builder().name(name).active(true).build());
        }
    }
}
