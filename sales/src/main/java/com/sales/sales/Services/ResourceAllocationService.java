package com.sales.sales.Services;

import com.sales.sales.Entity.ResourceAllocation;
import com.sales.sales.Repositories.ResourceAllocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceAllocationService {

    private final ResourceAllocationRepository repository;

    // Get all allocations
    public List<ResourceAllocation> getAllAllocations() {
        log.info("Fetching all resource allocations");
        return repository.findAll();
    }

    // Get allocation by ID
    public Optional<ResourceAllocation> getAllocationById(Long id) {
        log.info("Fetching allocation by ID: {}", id);
        return repository.findById(id);
    }

    // Create new allocation
    public ResourceAllocation addAllocation(ResourceAllocation allocation) {
        log.info("Adding new resource allocation: {}", allocation);
        
        // Set default values if not provided
        if (allocation.getStatus() == null) {
            allocation.setStatus("Active");
        }
        
        if (allocation.getCreatedAt() == null) {
            allocation.setCreatedAt(LocalDate.now());
        }
        
        return repository.save(allocation);
    }

    // Update allocation
    public ResourceAllocation updateAllocation(Long id, ResourceAllocation allocation) {
        log.info("Updating allocation with ID: {}", id);
        
        if (repository.existsById(id)) {
            allocation.setAllocationId(id);
            return repository.save(allocation);
        }
        
        log.warn("Allocation not found with ID: {}", id);
        return null;
    }

    // Delete allocation
    public void deleteAllocation(Long id) {
        log.info("Deleting allocation with ID: {}", id);
        repository.deleteById(id);
    }

    // Check if allocation exists
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}