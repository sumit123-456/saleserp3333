package com.sales.sales.Repositories;

import com.sales.sales.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    List<Role> findAllByOrderByRoleNameAsc();
    Optional<Role> findByRoleName(String roleName);
}
