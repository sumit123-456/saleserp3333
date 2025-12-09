package com.sales.sales.Controller;


import com.sales.sales.Entity.Employee;
import com.sales.sales.Services.EmployeeService;
import com.sales.sales.Services.impl.EmployeeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
   private EmployeeService employeeService;

    public EmployeeController(EmployeeService service) {
        this.employeeService = service;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public Optional<Employee> getEmployeeById(@PathVariable String id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    public Employee addEmployee(@RequestBody Employee emp) {
        return employeeService.addEmployee(emp);
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable String id, @RequestBody Employee emp) {
        return employeeService.updateEmployee(id, emp);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable String id) {
        employeeService.deleteEmployee(id);
    }

    @GetMapping("/filter")
    public List<Employee> filterEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer achieved) {
        return employeeService.filterEmployees(name, team, startDate, endDate, achieved);
    }

    @GetMapping("/sales-summary")
    public ResponseEntity<?> getSalesSummary() {
        try {
            Map<String, Object> summary = employeeService.getSalesSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch sales summary"));
        }
    }

//    @GetMapping("/sales/filter")
//    public ResponseEntity<?> filterSales(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String team,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//        return ResponseEntity.ok(((EmployeeServiceImpl) employeeService).filterSales(name, team, startDate, endDate));
//    }

    @GetMapping("/calls/summary")
    public ResponseEntity<?> getCallSummary() {
        try {
            Map<String, Object> summary = employeeService.getCallSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch call summary"));
        }
    }

    @GetMapping("/calls/top10")
    public ResponseEntity<?> getTop10EmployeesByCalls() {
        return ResponseEntity.ok(((EmployeeServiceImpl) employeeService).getTop10EmployeesByCalls());
    }

    @GetMapping("/sales-calling-overview")
    public ResponseEntity<?> getSalesCallingOverview() {
        try {
            Map<String, Object> overview = employeeService.getSalesCallingOverview();
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch  sales calling overview: " + e.getMessage()));
        }
    }
    @GetMapping("/performance/top-bottom")
    public ResponseEntity<?> getTopBottomPerformance() {
        try {
            Map<String, Object> performance = employeeService.getTopAndBottomEmployeesByPerformance();
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch performance data"));
        }
    }


    @GetMapping("/weekly-summary")
    public ResponseEntity<?> getWeeklySummary() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            List<Map<String, Object>> summary = new ArrayList<>();
            
            for (Employee emp : employees) {
                Map<String, Object> empData = new LinkedHashMap<>();
                empData.put("empId", emp.getEmpId());
                empData.put("name", emp.getEmpName());
                empData.put("team", emp.getTeam() != null ? emp.getTeam() : "N/A");
                empData.put("monthlyTarget", emp.getMonthlyTarget() != null ? emp.getMonthlyTarget() : 0);
                empData.put("achieved", emp.getAchieved() != null ? emp.getAchieved() : 0);
                empData.put("monthlyCallTarget", emp.getMonthlyCallTarget() != null ? emp.getMonthlyCallTarget() : 0);
                empData.put("callsMade", emp.getCallsMade() != null ? emp.getCallsMade() : 0);
                summary.add(empData);
            }
            
            return ResponseEntity.ok(Map.of("status", "success", "message", "Weekly summary fetched", "data", summary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Failed to fetch weekly summary: " + e.getMessage()));
        }
    }

    @GetMapping("/calls/disposition-summary")
    public ResponseEntity<List<Map<String, Object>>> getDispositionSummary() {
        List<Map<String, Object>> dispositionSummary = new ArrayList<>();

        // Example data
        dispositionSummary.add(Map.of("type", "Inbound Calls", "value", 120));
        dispositionSummary.add(Map.of("type", "Outbound Calls", "value", 80));
        dispositionSummary.add(Map.of("type", "Missed Calls", "value", 30));

        return ResponseEntity.ok(dispositionSummary);
    }



}








