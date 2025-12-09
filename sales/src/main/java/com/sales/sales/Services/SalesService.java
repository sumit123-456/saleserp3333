package com.sales.sales.Services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SalesService {

    public Map<String, Object> getSalesPerformance() {
        log.info("SalesService : getSalesPerformance() : Fetching sales performance data");

        Map<String, Object> performance = new HashMap<>();
        // Add sales performance metrics
        performance.put("totalRevenue", 456000.0);
        performance.put("averageDealSize", 2923.08);
        performance.put("conversionRate", 35.2);
        performance.put("dealsClosed", 156);

        return performance;
    }
}