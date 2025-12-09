package com.sales.sales.Services;

import com.sales.sales.Entity.CallDetail;
import com.sales.sales.Entity.Employee;
import com.sales.sales.Repositories.CallDetailRepository;
import com.sales.sales.Repositories.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class CallDetailService {

    private final CallDetailRepository repo;
    private final CallDetailRepository callRepo;
    private final EmployeeRepository employeeRepo;

    public CallDetailService(CallDetailRepository repo, CallDetailRepository callRepo, EmployeeRepository employeeRepo) {
        this.repo = repo;
        this.callRepo = callRepo;
        this.employeeRepo = employeeRepo;
    }

    // Get all call records
    public List<CallDetail> getAllCalls() {
        return repo.findAll();
    }

    // Get single call record by ID
    public Optional<CallDetail> getCallById(Long id) {
        return repo.findById(id);
    }

    // Add new call record
    public CallDetail addCall(CallDetail call) {
        return repo.save(call);
    }

    // Update existing call record
    public CallDetail updateCall(Long id, CallDetail call) {
        if (repo.existsById(id)) {
//            call.setCallId(id);
            return repo.save(call);
        }
        return null;
    }

    // Delete call record
    public void deleteCall(Long id) {
        repo.deleteById(id);
    }


    // ✅ API 1 - Filter calls by name/team/time range
    public List<CallDetail> filterCalls(String name, String team, String range) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (range == null ? "" : range.toLowerCase()) {
            case "today" -> endDate;
            case "7days" -> endDate.minusDays(7);
            case "14days" -> endDate.minusDays(14);
            case "30days" -> endDate.minusDays(30);
            default -> null;
        };
        return callRepo.filterCalls(name, team, startDate, endDate);
    }


    // ✅ API 2 - Summary (total calls, target, met, avg duration)
    public Map<String, Object> getCallSummary() {
        List<Employee> employees = employeeRepo.findAll();
        List<CallDetail> allCalls = callRepo.findAll();

        int totalCallTarget = employees.stream()
                .mapToInt(e -> Optional.ofNullable(e.getMonthlyCallTarget()).orElse(0))
                .sum();

        int totalCallsMade = allCalls.size();
        int targetMet = employees.stream()
                .mapToInt(e -> Optional.ofNullable(e.getMeetTarget()).orElse(0))
                .sum();

        // Average duration (in seconds → mm:ss)
        double avgSeconds = allCalls.isEmpty()
                ? 0
                : allCalls.stream().mapToInt(CallDetail::getDuration).average().orElse(0);
        int avgMin = (int) (avgSeconds / 60);
        int avgSec = (int) (avgSeconds % 60);
        String formattedAvg = String.format("%02d:%02d", avgMin, avgSec);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCallsMade", totalCallsMade);
        summary.put("totalCallTarget", totalCallTarget);
        summary.put("targetMet", targetMet);
        summary.put("averageDuration", formattedAvg);
        return summary;
    }



    // ✅ API 5 - Call Type Summary
    public Map<String, Long> getCallTypeSummary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (Object[] row : callRepo.countByCallType()) {
            summary.put(row[0].toString(), (Long) row[1]);
        }
        return summary;
    }

    // ✅ API 6 - Disposition Summary
    public Map<String, Long> getDispositionSummary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (Object[] row : callRepo.countByDisposition()) {
            summary.put(row[0].toString(), (Long) row[1]);
        }
        return summary;
    }



}
