package com.ai.sdlc.expensetracker.dashboard;

import com.ai.sdlc.expensetracker.dashboard.dto.DashboardResponse;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse get(@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month) {
        YearMonth ym = YearMonth.now();
        int y = year == null ? ym.getYear() : year;
        int m = month == null ? ym.getMonthValue() : month;
        return dashboardService.dashboard(y, m);
    }
}
