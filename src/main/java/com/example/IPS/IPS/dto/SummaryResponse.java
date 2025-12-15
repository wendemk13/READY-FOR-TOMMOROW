package com.example.IPS.IPS.dto;

import java.util.List;
import java.util.Map;

public record SummaryResponse(
        List<DailySummary> dailySummaries,
        long totalFailures,
        double totalAmount,
        Map<String, Long> failureByType
) {}
