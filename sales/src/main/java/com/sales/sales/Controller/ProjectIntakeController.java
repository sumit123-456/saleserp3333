package com.sales.sales.Controller;

import com.sales.sales.Entity.Employee;
import com.sales.sales.Entity.ProjectIntake;
import com.sales.sales.Services.impl.ProjectIntakeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "*") // Allow frontend access
public class ProjectIntakeController {

    @Autowired
    private ProjectIntakeServiceImpl service;

    @PostMapping("/add")
    public ProjectIntake addProject(@RequestBody ProjectIntake projectIntake) {

        if (projectIntake.getEmpId() == null) {
            throw new RuntimeException("empId is required");
        }

        Employee employee = service.findEmployeeById(projectIntake.getEmpId());
        if (employee == null) {
            throw new RuntimeException("Employee not found with empId: " + projectIntake.getEmpId());
        }

        projectIntake.setEmployee(employee);
        projectIntake.setEmployeeName(employee.getEmpName());

        return service.save(projectIntake);
    }

    @GetMapping("/all")
    public List<ProjectIntake> getAllProjects() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProjectIntake getProject(@PathVariable Long id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    public String deleteProject(@PathVariable Long id) {
        service.deleteById(id);
        return "Project deleted successfully!";
    }

    @GetMapping("/summary")
    public Map<String, Long> getProjectSummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("totalProjects", service.getTotalProjects());
        summary.put("activeProjects", service.getActiveProjects());
        summary.put("completedProjects", service.getCompletedProjects());
        summary.put("pendingProjects", service.getPendingProjects());
        return summary;
    }


    @GetMapping("/stats")
    public Map<String, Long> getProjectStats() {
        return service.getProjectStats();
    }

    // API 1: Intake date count
    @GetMapping("/over-time")
    public List<Map<String, Object>> getProjectsCountByAllIntakeDates() {
        return service.getProjectsCountByAllIntakeDates();
    }

    // API 2: Group by project type
    @GetMapping("/group-by-type")
    public List<Map<String, Object>> getProjectsGroupedByType() {
        return service.getProjectsGroupedByType();
    }

    // API 3: Group by project status
    @GetMapping("/group-by-status")
    public List<Map<String, Object>> getProjectsGroupedByStatus() {
        return service.getProjectsGroupedByStatus();
    }

    // API 4: Group by client/company name
    @GetMapping("/group-by-client")
    public List<Map<String, Object>> getProjectsGroupedByClient() {
        return service.getProjectsGroupedByClient();
    }

    @GetMapping("/filter")
    public List<ProjectIntake> filterProjects(
            @RequestParam(required = false) String projectType,
            @RequestParam(required = false) String projectStatus,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String dateRange  // today, last7, last14, last30
    ) {
        return service.filterProjects(projectType, projectStatus, companyName, dateRange);
    }

}

