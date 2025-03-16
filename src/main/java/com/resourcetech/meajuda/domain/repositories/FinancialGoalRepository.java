package com.resourcetech.meajuda.domain.repositories;

import com.resourcetech.meajuda.domain.entities.FinancialGoal;
import com.resourcetech.meajuda.domain.entities.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {
    List<FinancialGoal> findByUserId(Long userId);
    List<FinancialGoal> findByUserIdAndStatus(Long userId, GoalStatus status);
} 