package dev.diakon.diakonpay.controller;

import dev.diakon.diakonpay.dto.analytics.AnalyticsByCategoryResponseDto;
import dev.diakon.diakonpay.dto.analytics.AnalyticsTotalBalanceResponseDto;
import dev.diakon.diakonpay.dto.analytics.AnalyticsTotalsResponseDto;
import dev.diakon.diakonpay.service.AnalyticsService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/totals")
    public ResponseEntity<@NonNull AnalyticsTotalsResponseDto> getTotals(
            @RequestParam Integer userId,
            @RequestParam String period) {
        return ResponseEntity.ok(analyticsService.getTotals(userId, period));
    }

    @GetMapping("/by-category")
    public ResponseEntity<@NonNull List<AnalyticsByCategoryResponseDto>> getByCategory(
            @RequestParam Integer userId,
            @RequestParam String period) {
        return ResponseEntity.ok(analyticsService.getByCategory(userId, period));
    }

    @GetMapping("/total-balance")
    public ResponseEntity<@NonNull AnalyticsTotalBalanceResponseDto> getTotalBalance(
            @RequestParam Integer userId) {
        return ResponseEntity.ok(analyticsService.getTotalBalance(userId));
    }
}
