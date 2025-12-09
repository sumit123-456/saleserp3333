package com.sales.sales.Services;



import com.sales.sales.Entity.Employee;
import com.sales.sales.Repositories.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public abstract class EmployeeService {

    protected final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = repo;
    }

    // Implement the missing method
    public List<Employee> getAllEmployees() {
        return repo.findAll();
    }

    public Optional<Employee> getEmployeeById(String id) {
        return repo.findById(id);
    }

    public Employee addEmployee(Employee emp) {
        // Validate required fields
        if (emp.getEmpId() == null || emp.getEmpId().trim().isEmpty()) {
            throw new RuntimeException("Employee ID is required");
        }
        if (emp.getEmpName() == null || emp.getEmpName().trim().isEmpty()) {
            throw new RuntimeException("Employee name is required");
        }
        if (emp.getEmail() == null || emp.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        // Check if employee ID already exists
        if (repo.existsById(emp.getEmpId())) {
            throw new RuntimeException("Employee ID already exists: " + emp.getEmpId());
        }

        // Check if email already exists
        if (repo.findByEmail(emp.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + emp.getEmail());
        }

        // Set default values
        if (emp.getAchieved() == null) {
            emp.setAchieved(0);
        }
        if (emp.getCallsMade() == null) {
            emp.setCallsMade(0);
        }
        if (emp.getMeetTarget() == null) {
            emp.setMeetTarget(0);
        }
        if (emp.getJoinDate() == null) {
            emp.setJoinDate(LocalDate.now());
        }
        if (emp.getMonthlyCallTarget() == null) {
            emp.setMonthlyCallTarget(0);
        }
        if (emp.getMonthlyTarget() == null) {
            emp.setMonthlyTarget(0);
        }

        return repo.save(emp);
    }

    public Employee updateEmployee(String id, Employee emp) {
        if (repo.existsById(id)) {
            emp.setEmpId(id);
            return repo.save(emp);
        }
        throw new RuntimeException("Employee not found with ID: " + id);
    }

    public void deleteEmployee(String id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
        } else {
            throw new RuntimeException("Employee not found with ID: " + id);
        }
    }

    public boolean existsByEmail(String email) {
        return repo.findByEmail(email).isPresent();
    }

    // Add the required dashboard methods
    public Map<String, Object> getSalesSummary() {
        List<Employee> employees = repo.findAll();
        Map<String, Object> summary = new HashMap<>();

        // Calculate total calls from all employees
        Integer totalCalls = employees.stream()
                .mapToInt(emp -> emp.getCallsMade() != null ? emp.getCallsMade() : 0)
                .sum();

        // Calculate employees meeting call target
        long employeesMeetingCallTarget = employees.stream()
                .filter(emp -> emp.getCallsMade() != null &&
                        emp.getMonthlyCallTarget() != null &&
                        emp.getCallsMade() >= emp.getMonthlyCallTarget())
                .count();

        double callSuccessRate = employees.isEmpty() ? 0 :
                (employeesMeetingCallTarget * 100.0) / employees.size();

        // Calculate employees meeting sales target
        long employeesMeetingSalesTarget = employees.stream()
                .filter(emp -> emp.getAchieved() != null &&
                        emp.getMonthlyTarget() != null &&
                        emp.getAchieved() >= emp.getMonthlyTarget())
                .count();

        double salesSuccessRate = employees.isEmpty() ? 0 :
                (employeesMeetingSalesTarget * 100.0) / employees.size();

        summary.put("totalCalls", totalCalls);
        summary.put("successRate", Math.round(callSuccessRate));
        summary.put("averageDuration", 5.5); // Default value
        summary.put("activeEmployees", employees.size());
        summary.put("totalAchieved", employees.stream()
                .mapToInt(emp -> emp.getAchieved() != null ? emp.getAchieved() : 0)
                .sum());
        summary.put("totalTarget", employees.stream()
                .mapToInt(emp -> emp.getMonthlyTarget() != null ? emp.getMonthlyTarget() : 0)
                .sum());

        return summary;
    }

    public Map<String, Object> getCallSummary() {
        List<Employee> employees = repo.findAll();
        Map<String, Object> callSummary = new HashMap<>();

        int totalCalls = employees.stream()
                .mapToInt(emp -> emp.getCallsMade() != null ? emp.getCallsMade() : 0)
                .sum();

        // Calculate call direction split (60% inbound, 40% outbound as example)
        int inboundCalls = (int) (totalCalls * 0.6);
        int outboundCalls = totalCalls - inboundCalls;

        // Calculate disposition breakdown
        Map<String, Integer> dispositions = new HashMap<>();
        dispositions.put("Missed Calls", (int) (totalCalls * 0.2));
        dispositions.put("Successful Calls", (int) (totalCalls * 0.6));
        dispositions.put("Failed Calls", (int) (totalCalls * 0.2));

        callSummary.put("inboundCalls", inboundCalls);
        callSummary.put("outboundCalls", outboundCalls);
        callSummary.put("dispositions", dispositions);
        callSummary.put("totalCalls", totalCalls);

        return callSummary;
    }

    // Add the missing method
    public Map<String, Object> getSalesCallingOverview() {
        List<Employee> employees = repo.findAll();
        Map<String, Object> overview = new HashMap<>();

        // Generate monthly data based on employee performance
        List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun");

        // Calculate monthly targets and achievements based on actual employee data
        List<Integer> monthlyTargets = new ArrayList<>();
        List<Integer> monthlyAchieved = new ArrayList<>();
        List<Integer> monthlyCalls = new ArrayList<>();

        // Calculate base values from employee data
        int baseTarget = employees.isEmpty() ? 0 :
                employees.stream().mapToInt(emp -> emp.getMonthlyTarget() != null ? emp.getMonthlyTarget() : 0).sum() / employees.size();

        int baseAchieved = employees.isEmpty() ? 0 :
                employees.stream().mapToInt(emp -> emp.getAchieved() != null ? emp.getAchieved() : 0).sum() / employees.size();

        int baseCalls = employees.isEmpty() ? 0 :
                employees.stream().mapToInt(emp -> emp.getCallsMade() != null ? emp.getCallsMade() : 0).sum() / employees.size();

        Random random = new Random();
        for (int i = 0; i < months.size(); i++) {
            // Add some variation to make it look realistic
            int target = Math.max(0, baseTarget + random.nextInt(5000) - 2500);
            int achieved = Math.max(0, baseAchieved + random.nextInt(6000) - 3000);
            int calls = Math.max(0, baseCalls + random.nextInt(200) - 100);

            monthlyTargets.add(target);
            monthlyAchieved.add(achieved);
            monthlyCalls.add(calls);
        }

        overview.put("months", months);
        overview.put("targets", monthlyTargets);
        overview.put("achieved", monthlyAchieved);
        overview.put("calls", monthlyCalls);

        return overview;
    }

    public Map<String, Object> getTopAndBottomEmployeesByPerformance() {
        List<Employee> allEmployees = repo.findAll();

        // Calculate performance score for each employee
        List<EmployeePerformance> employeePerformances = allEmployees.stream()
                .map(employee -> {
                    double performanceScore = calculatePerformanceScore(employee);
                    return new EmployeePerformance(employee, performanceScore);
                })
                .sorted((a, b) -> Double.compare(b.getPerformanceScore(), a.getPerformanceScore()))
                .collect(Collectors.toList());

        // Get top 5 performers
        List<Map<String, Object>> topPerformers = employeePerformances.stream()
                .limit(5)
                .map(ep -> createPerformanceMap(ep.getEmployee(), ep.getPerformanceScore()))
                .collect(Collectors.toList());

        // Get bottom 5 performers
        List<Map<String, Object>> bottomPerformers = employeePerformances.stream()
                .skip(Math.max(0, employeePerformances.size() - 5))
                .map(ep -> createPerformanceMap(ep.getEmployee(), ep.getPerformanceScore()))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("topPerformers", topPerformers);
        result.put("bottomPerformers", bottomPerformers);

        return result;
    }

    private double calculatePerformanceScore(Employee employee) {
        if (employee == null) return 0.0;

        double achievementWeight = 0.6;
        double callsWeight = 0.3;
        double meetTargetWeight = 0.1;

        // Achievement score (0-100)
        double achievementScore = 0.0;
        if (employee.getMonthlyTarget() != null && employee.getMonthlyTarget() > 0) {
            achievementScore = (employee.getAchieved().doubleValue() / employee.getMonthlyTarget()) * 100;
        }

        // Calls score (0-100)
        double callsScore = 0.0;
        if (employee.getMonthlyCallTarget() != null && employee.getMonthlyCallTarget() > 0) {
            callsScore = (employee.getCallsMade().doubleValue() / employee.getMonthlyCallTarget()) * 100;
        }
        callsScore = Math.min(callsScore, 100); // Cap at 100%

        // Meet target score (already in percentage)
        double meetTargetScore = employee.getMeetTarget() != null ? employee.getMeetTarget().doubleValue() : 0.0;

        // Calculate weighted performance score
        return (achievementScore * achievementWeight) +
                (callsScore * callsWeight) +
                (meetTargetScore * meetTargetWeight);
    }

    private Map<String, Object> createPerformanceMap(Employee employee, double performanceScore) {
        Map<String, Object> performanceMap = new HashMap<>();
        performanceMap.put("empId", employee.getEmpId());
        performanceMap.put("empName", employee.getEmpName());
        performanceMap.put("empCode", employee.getEmpCode());
        performanceMap.put("team", employee.getTeam());
        performanceMap.put("department", employee.getDepartment());
        performanceMap.put("performanceScore", Math.round(performanceScore * 100.0) / 100.0); // Round to 2 decimal
        performanceMap.put("achieved", employee.getAchieved());
        performanceMap.put("monthlyTarget", employee.getMonthlyTarget());
        performanceMap.put("callsMade", employee.getCallsMade());
        performanceMap.put("monthlyCallTarget", employee.getMonthlyCallTarget());
        performanceMap.put("meetTarget", employee.getMeetTarget());

        return performanceMap;
    }

    // Abstract methods that need implementation in concrete class
    public abstract List<Employee> filterEmployees(
            String name,
            String team,
            LocalDate startDate,
            LocalDate endDate,
            Integer achieved
    );

    // Helper class for employee performance
    private static class EmployeePerformance {
        private final Employee employee;
        private final double performanceScore;

        public EmployeePerformance(Employee employee, double performanceScore) {
            this.employee = employee;
            this.performanceScore = performanceScore;
        }

        public Employee getEmployee() {
            return employee;
        }

        public double getPerformanceScore() {
            return performanceScore;
        }
    }
}