package com.sales.sales.Controller;



import com.sales.sales.Services.DashboardService;
import com.sales.sales.validation.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        log.info("DashboardController : getDashboardStats() : Execution Start");
        try {
            Map<String, Object> stats = dashboardService.getDashboardStats();
            log.info("Dashboard stats returned successfully");
            return CommonUtil.createBuildResponse(stats, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting dashboard stats: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Failed to fetch dashboard statistics", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sales-overview")
    public ResponseEntity<?> getSalesOverview() {
        log.info("DashboardController : getSalesOverview() : Execution Start");
        try {
            Map<String, Object> salesOverview = dashboardService.getSalesOverview();
            log.info("Sales overview data returned successfully");
            return CommonUtil.createBuildResponse(salesOverview, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting sales overview: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Failed to fetch sales overview", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/recent-sales")
    public ResponseEntity<?> getRecentSales() {
        log.info("DashboardController : getRecentSales() : Execution Start");
        try {
            List<Map<String, Object>> recentSales = dashboardService.getRecentSales();
            log.info("Recent sales returned: {} records", recentSales.size());
            return CommonUtil.createBuildResponse(recentSales, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting recent sales: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Failed to fetch recent sales", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/top-performers")
    public ResponseEntity<?> getTopPerformers() {
        log.info("DashboardController : getTopPerformers() : Execution Start");
        try {
            List<Map<String, Object>> topPerformers = dashboardService.getTopPerformers();
            log.info("Top performers returned: {} records", topPerformers.size());

            // Ensure we return exactly top 10
            List<Map<String, Object>> top10Performers = topPerformers.stream()
                    .limit(10)
                    .collect(Collectors.toList());

            log.info("Limited to top {} performers", top10Performers.size());
            return CommonUtil.createBuildResponse(top10Performers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting top performers: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Failed to fetch top performers", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee-performance")
    public ResponseEntity<?> getEmployeePerformance() {
        log.info("DashboardController : getEmployeePerformance() : Execution Start");
        try {
            Map<String, Object> performance = dashboardService.getEmployeePerformance();
            log.info("Employee performance data returned successfully");
            return CommonUtil.createBuildResponse(performance, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting employee performance: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Failed to fetch employee performance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/counts")
    public ResponseEntity<?> getDashboardCounts() {
        log.info("DashboardController : getDashboardCounts() : Execution Start");
        try {
            Map<String, Object> counts = dashboardService.getDashboardCounts();
            log.info("Dashboard counts returned successfully");
            return CommonUtil.createBuildResponse(counts, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting dashboard counts: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Failed to fetch dashboard counts", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/complete")
    public ResponseEntity<?> getCompleteDashboardData() {
        log.info("DashboardController : getCompleteDashboardData() : Execution Start");
        try {
            Map<String, Object> completeData = new HashMap<>();

            // Get all dashboard data
            Map<String, Object> stats = dashboardService.getDashboardStats();
            Map<String, Object> counts = dashboardService.getDashboardCounts();
            List<Map<String, Object>> topPerformers = dashboardService.getTopPerformers();
            List<Map<String, Object>> recentSales = dashboardService.getRecentSales();
            Map<String, Object> performance = dashboardService.getEmployeePerformance();
            Map<String, Object> overview = dashboardService.getSalesOverview();

            completeData.put("stats", stats);
            completeData.put("counts", counts);
            completeData.put("topPerformers", topPerformers);
            completeData.put("recentSales", recentSales);
            completeData.put("performance", performance);
            completeData.put("overview", overview);

            log.info("Complete dashboard data returned successfully");
            return CommonUtil.createBuildResponse(completeData, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting complete dashboard data: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Failed to fetch complete dashboard data", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}