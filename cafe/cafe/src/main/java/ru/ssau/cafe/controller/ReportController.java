package ru.ssau.cafe.controller;

import ru.ssau.cafe.dto.ReportDto;
import ru.ssau.cafe.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily")
    public ResponseEntity<ReportDto> getDailyReport(@RequestParam LocalDate date) {
        return ResponseEntity.ok(reportService.getDailyReport(date));
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<ReportDto>> getWeeklyReport(@RequestParam LocalDate startDate) {
        return ResponseEntity.ok(reportService.getWeeklyReport(startDate));
    }

    @GetMapping("/popular-items")
    public ResponseEntity<Map<String, Integer>> getPopularItems() {
        return ResponseEntity.ok(reportService.getPopularItems());
    }

    @GetMapping("/sales-by-category")
    public ResponseEntity<Map<String, Long>> getSalesByCategory() {
        return ResponseEntity.ok(reportService.getSalesByCategory());
    }
}