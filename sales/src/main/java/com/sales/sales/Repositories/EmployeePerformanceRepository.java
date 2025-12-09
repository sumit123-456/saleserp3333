package com.sales.sales.Repositories;

import com.sales.sales.Entity.EmployeePerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org
        .springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeePerformanceRepository extends JpaRepository<EmployeePerformance, Long> {

    @Query("SELECT ep FROM EmployeePerformance ep ORDER BY ep.performanceScore DESC")
    List<EmployeePerformance> findTopPerformers();
}