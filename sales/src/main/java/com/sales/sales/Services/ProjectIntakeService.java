package com.sales.sales.Services;

import com.sales.sales.Entity.ProjectIntake;

import java.util.List;
import java.util.Map;

public interface ProjectIntakeService {
    ProjectIntake save(ProjectIntake project);
    List<ProjectIntake> findAll();
    ProjectIntake findById(Long id);
    void deleteById(Long id);
    long countByStatus(String status);


    Map<String, Long> getProjectStats();

    long getTotalProjects();

    long getActiveProjects();

    long getCompletedProjects();

    long getPendingProjects();


}