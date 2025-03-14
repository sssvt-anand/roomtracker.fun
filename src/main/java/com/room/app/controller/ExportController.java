package com.room.app.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.room.app.service.ExportService;

@RestController
@RequestMapping("/api/exports")
@CrossOrigin(origins = "http://localhost:3000")
public class ExportController {

    private final ExportService exportService;

    @Autowired
    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> exportMonthly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return exportService.exportMonthlyExpenses(start, end);
    }

    @GetMapping("/yearly")
    public ResponseEntity<?> exportYearly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return exportService.exportYearlyExpenses(start, end);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> exportByMember(@PathVariable Long memberId) {
        return exportService.exportExpensesByMember(memberId);
    }

    @GetMapping("/general")
    public ResponseEntity<?> exportGeneral() {
        return exportService.exportExpensesWithoutMember();
    }

    @GetMapping("/history")
    public ResponseEntity<?> getExportHistory() {
        return ResponseEntity.ok(exportService.getExportHistory());
    }
}