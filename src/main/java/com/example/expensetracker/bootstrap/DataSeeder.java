package com.example.expensetracker.bootstrap;

import com.example.expensetracker.categories.Category;
import com.example.expensetracker.categories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) return;

        List<String> predefined = List.of(
                "Food",
                "Transport",
                "Rent",
                "Subscriptions",
                "Utilities",
                "Shopping",
                "Health",
                "Entertainment",
                "Other"
        );

        for (String name : predefined) {
            Category c = new Category();
            c.setName(name);
            c.setSystemDefined(true);
            categoryRepository.save(c);
        }
    }
}
