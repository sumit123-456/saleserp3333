package com.sales.sales.Services.impl;

import com.sales.sales.Entity.CallDetail;
import com.sales.sales.Entity.Employee;
import com.sales.sales.Repositories.CallDetailRepository;
import com.sales.sales.Repositories.EmployeeRepository;
import com.sales.sales.Services.EmployeeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl extends EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CallDetailRepository callRepo;

    public EmployeeServiceImpl(EmployeeRepository employeeRepo, CallDetailRepository callRepo, EmployeeRepository employeeRepository) {
        super(employeeRepo);
        this.employeeRepository = employeeRepo;
        this.callRepo = callRepo;
//        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<Employee> filterEmployees(String name, String team, LocalDate startDate, LocalDate endDate, Integer achieved) {
        return employeeRepository.filterEmployees(name, team, startDate, endDate, achieved);
    }

    public Map<String, Object> getSalesSummary() {
        List<Employee> employees = employeeRepository.findAll();
        int totalTarget = employees.stream().mapToInt(e -> Optional.ofNullable(e.getMonthlyTarget()).orElse(0)).sum();
        int totalAchieved = employees.stream().mapToInt(e -> Optional.ofNullable(e.getAchieved()).orElse(0)).sum();
        double percentage = totalTarget == 0 ? 0 : ((double) totalAchieved / totalTarget) * 100;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSalesTarget", totalTarget);
        summary.put("totalAchieved", totalAchieved);
        summary.put("achievementPercentage", percentage);
        return summary;
    }

    public List<CallDetail> filterSales(String name, String team, LocalDate startDate, LocalDate endDate) {
        return callRepo.filterCalls(name, team, startDate, endDate);
    }

    public Map<String, Object> getCallSummary() {
        List<Employee> employees = employeeRepository.findAll();
        List<CallDetail> connectedCalls = callRepo.findConnectedCalls();



        int totalCallTarget = employees.stream().mapToInt(e -> Optional.ofNullable(e.getMonthlyCallTarget()).orElse(0)).sum();
        int connectedCount = connectedCalls.size();
        double percentage = totalCallTarget == 0 ? 0 : ((double) connectedCount / totalCallTarget) * 100;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCallTarget", totalCallTarget);
        summary.put("connectedCalls", connectedCount);
        summary.put("connectedPercentage", percentage);
        return summary;

    }

    public List<Map<String, Object>> getTop10EmployeesByCalls() {
        List<Object[]> result = callRepo.findTop10ByCallsMade();
        List<Map<String, Object>> topEmployees = new ArrayList<>();

        for (Object[] row : result) {
            String empId = (String) row[0];
            Long count = (Long) row[1];
            employeeRepository.findById(empId).ifPresent(emp -> {
                Map<String, Object> data = new HashMap<>();
                data.put("employeeName", emp.getEmpName());
                data.put("callsMade", count);
                data.put("team", emp.getDepartment());
                topEmployees.add(data);
            });
        }
        return topEmployees;
    }

    @Override
    public Map<String, Object> getTopAndBottomEmployeesByPerformance() {
        List<Employee> allEmployees = employeeRepository.findAll();

        // Calculate performance percentage for each employee
        List<Map<String, Object>> employeePerformance = allEmployees.stream()
                .map(e -> {
                    double monthlyTarget = Optional.ofNullable(e.getMonthlyTarget()).orElse(0);
                    double meetTarget = Optional.ofNullable(e.getMeetTarget()).orElse(0);
                    double percentage = (monthlyTarget > 0) ? (meetTarget / monthlyTarget) * 100 : 0;

                    Map<String, Object> data = new HashMap<>();
                    data.put("empId", e.getEmpId());
                    data.put("empName", e.getEmpName());
                    data.put("team", e.getDepartment());
                    data.put("monthlyTarget", monthlyTarget);
                    data.put("meetTarget", meetTarget);
                    data.put("performancePercentage", Math.round(percentage * 100.0) / 100.0);

                    return data;
                })
                .collect(Collectors.toList());

        // Sort employees by percentage (descending for top, ascending for bottom)
        List<Map<String, Object>> sorted = employeePerformance.stream()
                .sorted((a, b) -> Double.compare((double) b.get("performancePercentage"), (double) a.get("performancePercentage")))
                .collect(Collectors.toList());

        // Get top 5 and bottom 5
        List<Map<String, Object>> top5 = sorted.stream().limit(5).collect(Collectors.toList());
        List<Map<String, Object>> bottom5 = sorted.stream()
                .skip(Math.max(0, sorted.size() - 5))
                .collect(Collectors.toList());

        // Combine results
        Map<String, Object> result = new HashMap<>();
        result.put("top5Employees", top5);
        result.put("bottom5Employees", bottom5);

        return result;
    }
}
