package com.sales.sales.Repositories;

import com.sales.sales.Entity.SalesPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesPerformanceRepository extends JpaRepository<SalesPerformance, Long> {

    @Query("SELECT sp FROM SalesPerformance sp WHERE sp.monthYear = :monthYear ORDER BY sp.salesAchieved DESC")
    List<SalesPerformance> findTopPerformersByMonth(String monthYear);

    @Query("SELECT SUM(sp.salesAchieved) FROM SalesPerformance sp WHERE sp.monthYear = :monthYear")
    Double getTotalSalesByMonth(String monthYear);
}

