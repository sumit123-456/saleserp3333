package com.sales.sales.Services;

import com.sales.sales.Entity.Employee;
import com.sales.sales.Entity.LeadDetail;
import com.sales.sales.Repositories.EmployeeRepository;
import com.sales.sales.Repositories.LeadDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeadDetailService {

    private final LeadDetailRepository repo;
    private final EmployeeRepository employeeRepo;

    @Autowired
    public LeadDetailService(LeadDetailRepository repo, EmployeeRepository employeeRepo) {
        this.repo = repo;
        this.employeeRepo = employeeRepo;
    }

    // 1. Get all leads
    public List<LeadDetail> getAllLeads() {
        return repo.findAll();
    }

    // 2. Get lead by ID
    public Optional<LeadDetail> getLeadById(Long id) {
        return repo.findById(id);
    }

    // 3. Add lead
    public LeadDetail addLead(LeadDetail lead) {
        return repo.save(lead);
    }

    // 4. Update lead
    public LeadDetail updateLead(Long id, LeadDetail lead) {
        if (repo.existsById(id)) {
            lead.setLeadId(id);
            return repo.save(lead);
        }
        return null;
    }

    // 5. Delete lead
    public void deleteLead(Long id) {
        repo.deleteById(id);
    }

    // 6. Summary (total, conversion, average deal)
    public Map<String, Object> getSummary() {
        List<LeadDetail> leads = repo.findAll();

        long totalLeads = leads.size();
        long convertedCount = leads.stream()
                .filter(l -> "Yes".equalsIgnoreCase(l.getConvertedToDeal()))
                .count();

        double totalDealValue = leads.stream()
                .filter(l -> "Yes".equalsIgnoreCase(l.getConvertedToDeal()))
                .mapToDouble(l -> Optional.ofNullable(l.getDealValue()).orElse(0f))
                .sum();

        double conversionRate = totalLeads > 0 ? (convertedCount * 100.0) / totalLeads : 0.0;
        double averageDealValue = convertedCount > 0 ? totalDealValue / convertedCount : 0.0;

        return Map.of(
                "totalLeads", totalLeads,
                "totalDealValue", totalDealValue,
                "conversionRate", conversionRate,
                "averageDealValue", averageDealValue
        );
    }

    // 7. Leads per Team
    public Map<String, Long> leadsPerTeam() {
        List<LeadDetail> leads = repo.findAll();

        return leads.stream()
                .map(lead -> {
                    String team = employeeRepo.findById(String.valueOf(lead.getEmpId()))
                            .map(Employee::getTeam)
                            .orElse("Unknown");
                    return Map.entry(team, lead);
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.counting()
                ));
    }

    // 8. Leads per Day
    public Map<String, Long> leadsPerDay() {
        List<LeadDetail> leads = repo.findAll();

        return leads.stream()
                .filter(l -> l.getCreatedDate() != null) // ✅ prevent NullPointerException
                .collect(Collectors.groupingBy(
                        l -> l.getCreatedDate().toString(), // yyyy-MM-dd
                        Collectors.counting()
                ));
    }

    // 9. Lead Source Breakdown
    public Map<String, Long> sourceBreakdown() {
        List<LeadDetail> leads = repo.findAll();
        return leads.stream()
                .collect(Collectors.groupingBy(
                        l -> Optional.ofNullable(l.getSource()).orElse("Unknown"),
                        Collectors.counting()
                ));
    }

    // 10. Conversion by Team
    public Map<String, Double> conversionByTeam(Map<Long, String> empTeamMap) {
        List<LeadDetail> leads = repo.findAll();

        Map<String, List<LeadDetail>> byTeam = leads.stream()
                .collect(Collectors.groupingBy(
                        l -> empTeamMap.getOrDefault(l.getEmpId(), "Unknown")
                ));

        Map<String, Double> result = new HashMap<>();
        byTeam.forEach((team, list) -> {
            long total = list.size();
            long converted = list.stream()
                    .filter(l -> "Yes".equalsIgnoreCase(l.getConvertedToDeal()))
                    .count();
            double rate = total > 0 ? (converted * 100.0) / total : 0.0;
            result.put(team, rate);
        });

        return result;
    }
}


