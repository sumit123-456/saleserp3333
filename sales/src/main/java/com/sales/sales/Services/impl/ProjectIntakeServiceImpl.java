package com.sales.sales.Services.impl;
import com.sales.sales.Entity.Employee;
import com.sales.sales.Entity.ProjectIntake;
import com.sales.sales.Repositories.EmployeeRepository;
import com.sales.sales.Repositories.ProjectIntakeRepository;
import com.sales.sales.Services.ProjectIntakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectIntakeServiceImpl implements ProjectIntakeService {

    @Autowired
    private ProjectIntakeRepository repo;

    public ProjectIntakeServiceImpl(ProjectIntakeRepository projectRepository) {
        this.repo = projectRepository;
    }

    @Override
    public ProjectIntake save(ProjectIntake project) {
        return repo.save(project);
    }

    @Override
    public List<ProjectIntake> findAll() {
        return repo.findAll();
    }

    @Override
    public ProjectIntake findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public long countByStatus(String status) {
        return 0;
    }

    @Override
    public Map<String, Long> getProjectStats() {
        return Map.of();
    }

    @Override
    public long getTotalProjects() {
        return repo.count();
    }

    @Override
    public long getActiveProjects() {
        return repo.countByProjectStatus("Active");
    }

    @Override
    public long getCompletedProjects() {
        return repo.countByProjectStatus("Completed");
    }

    @Override
    public long getPendingProjects() {
        return repo.countByProjectStatus("Pending");
    }

    public List<Map<String, Object>> getProjectsCountByAllIntakeDates() {
        List<Object[]> results = repo.countProjectsGroupByIntakeDate();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("intakeDate", row[0]);
            map.put("count", row[1]);
            response.add(map);
        }

        return response;
    }

    // API 2
    public List<Map<String, Object>> getProjectsGroupedByType() {
        List<Object[]> results = repo.countProjectsGroupByType();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("projectType", row[0]);
            map.put("count", row[1]);
            response.add(map);
        }
        return response;
    }

    // API 3
    public List<Map<String, Object>> getProjectsGroupedByStatus() {
        List<Object[]> results = repo.countProjectsGroupByStatus();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("projectStatus", row[0]);
            map.put("count", row[1]);
            response.add(map);
        }
        return response;
    }

    // API 4
    public List<Map<String, Object>> getProjectsGroupedByClient() {
        List<Object[]> results = repo.countProjectsGroupByClient();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("companyName", row[0]);
            map.put("count", row[1]);
            response.add(map);
        }
        return response;
    }

    public List<ProjectIntake> filterProjects(String projectType, String projectStatus, String companyName, String dateRange) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        LocalDate startDate = null;

        // Calculate startDate based on dateRange
        if (dateRange != null) {
            switch (dateRange.toLowerCase()) {
                case "today":
                    startDate = today;
                    break;
                case "last7":
                    startDate = today.minusDays(7);
                    break;
                case "last14":
                    startDate = today.minusDays(14);
                    break;
                case "last30":
                    startDate = today.minusDays(30);
                    break;
            }
        }

        List<ProjectIntake> projects = repo.findAll();

        LocalDate finalStartDate = startDate;
        return projects.stream()
                .filter(p -> projectType == null || p.getProjectType().equalsIgnoreCase(projectType))
                .filter(p -> projectStatus == null || p.getProjectStatus().equalsIgnoreCase(projectStatus))
                .filter(p -> companyName == null || p.getCompanyName().equalsIgnoreCase(companyName))
                .filter(p -> {
                    if (finalStartDate == null) return true;
                    LocalDate intakeDate = LocalDate.parse(p.getIntakeDate(), formatter);
                    if (dateRange.equalsIgnoreCase("today")) {
                        return intakeDate.isEqual(today);
                    } else {
                        return !intakeDate.isBefore(finalStartDate) && !intakeDate.isAfter(today);
                    }
                })
                .toList();
    }
    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee findEmployeeById(String empId) {
        return employeeRepository.findById(empId).orElse(null);
    }
}
