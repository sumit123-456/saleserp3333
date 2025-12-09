package com.sales.sales.Controller;

import com.sales.sales.Entity.LeadDetail;
import com.sales.sales.Repositories.EmployeeRepository;
import com.sales.sales.Services.LeadDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/leads")
@CrossOrigin(origins = "*")
public class LeadDetailsController {

    @Autowired
    private LeadDetailService service;

    @Autowired
    private EmployeeRepository employeeRepo;

    public LeadDetailsController(LeadDetailService service) {
        this.service = service;
    }

    @GetMapping
    public List<LeadDetail> getAllLeads() {
        return service.getAllLeads();
    }

    @GetMapping("/{id}")
    public Optional<LeadDetail> getLeadById(@PathVariable Long id) {
        return service.getLeadById(id);
    }

    @PostMapping
    public LeadDetail addLead(@RequestBody LeadDetail lead) {
        return service.addLead(lead);
    }

    @PutMapping("/{id}")
    public LeadDetail updateLead(@PathVariable Long id, @RequestBody LeadDetail lead) {
        return service.updateLead(id, lead);
    }

    @DeleteMapping("/{id}")
    public void deleteLead(@PathVariable Long id) {
        service.deleteLead(id);
    }



    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        return service.getSummary();
    }

    @GetMapping("/leads-per-team")
    public Map<String, Long> leadsPerTeam() {
        return service.leadsPerTeam();
    }

    @GetMapping("/leads-per-day")
    public Map<String, Long> leadsPerDay() {
        return service.leadsPerDay();
    }

    @GetMapping("/source-breakdown")
    public Map<String, Long> sourceBreakdown() {
        return service.sourceBreakdown();
    }

    @GetMapping("/conversion")
    public Map<String, Integer> getLeadConversion() {

        Map<String, Integer> conversion = new LinkedHashMap<>();

        conversion.put("Team A", 45);
        conversion.put("Team B", 32);
        conversion.put("Team C", 60);
        conversion.put("Team D", 52);

        return conversion;
    }

}
