package com.sales.sales.Repositories;

import com.sales.sales.Entity.ProjectIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectIntakeRepository extends JpaRepository<ProjectIntake, Long> {

    @Query("SELECT COUNT(p) FROM ProjectIntake p")
    long countTotalProjects();

    // Active projects count
    @Query("SELECT COUNT(p) FROM ProjectIntake p WHERE p.projectStatus = 'Active'")
    long countActiveProjects();

    // Completed projects count
    @Query("SELECT COUNT(p) FROM ProjectIntake p WHERE p.projectStatus = 'Completed'")
    long countCompletedProjects();

    // Pending projects count
    @Query("SELECT COUNT(p) FROM ProjectIntake p WHERE p.projectStatus = 'Pending'")
    long countPendingProjects();

    long countByProjectStatus(String projectStatus);

    // API 1: count projects by intake date
    @Query("SELECT p.intakeDate, COUNT(p) FROM ProjectIntake p GROUP BY p.intakeDate")
    List<Object[]> countProjectsGroupByIntakeDate();

    // API 2: group by project type
    @Query("SELECT p.projectType, COUNT(p) FROM ProjectIntake p GROUP BY p.projectType")
    List<Object[]> countProjectsGroupByType();

    // API 3: group by project status
    @Query("SELECT p.projectStatus, COUNT(p) FROM ProjectIntake p GROUP BY p.projectStatus")
    List<Object[]> countProjectsGroupByStatus();

    // API 4: group by company name (client)
    @Query("SELECT p.companyName, COUNT(p) FROM ProjectIntake p GROUP BY p.companyName")
    List<Object[]> countProjectsGroupByClient();

}
