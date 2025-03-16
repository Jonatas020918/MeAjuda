package com.resourcetech.meajuda.presentation.controllers;

import com.resourcetech.meajuda.application.dtos.BudgetDto;
import com.resourcetech.meajuda.domain.entities.Budget;
import com.resourcetech.meajuda.domain.entities.User;
import com.resourcetech.meajuda.domain.repositories.BudgetRepository;
import com.resourcetech.meajuda.domain.repositories.UserRepository;
import com.resourcetech.meajuda.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public BudgetController(BudgetRepository budgetRepository, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    private BudgetDto convertToDto(Budget budget) {
        var dto = new BudgetDto();
        dto.setId(budget.getId());
        dto.setName(budget.getName());
        dto.setAmount(budget.getAmount());
        dto.setCategory(budget.getCategory());
        dto.setStartDate(budget.getStartDate());
        dto.setEndDate(budget.getEndDate());
        dto.setCurrentSpent(budget.getCurrentSpent());
        return dto;
    }

    private Budget convertToEntity(BudgetDto dto, User user) {
        var budget = new Budget();
        budget.setId(dto.getId());
        budget.setName(dto.getName());
        budget.setAmount(dto.getAmount());
        budget.setCategory(dto.getCategory());
        budget.setStartDate(dto.getStartDate());
        budget.setEndDate(dto.getEndDate());
        budget.setCurrentSpent(dto.getCurrentSpent());
        budget.setUser(user);
        return budget;
    }

    @GetMapping
    public List<BudgetDto> getBudgets(@AuthenticationPrincipal UserPrincipal currentUser) {
        return budgetRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<BudgetDto> createBudget(
            @Valid @RequestBody BudgetDto budgetDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        var user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        var budget = convertToEntity(budgetDto, user);
        var savedBudget = budgetRepository.save(budget);
        return ResponseEntity.ok(convertToDto(savedBudget));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetDto> getBudget(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return budgetRepository.findById(id)
                .<ResponseEntity<BudgetDto>>map(budget -> {
                    if (!budget.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(convertToDto(budget));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDto budgetDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return budgetRepository.findById(id)
                .<ResponseEntity<BudgetDto>>map(budget -> {
                    if (!budget.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.notFound().build();
                    }
                    budget.setName(budgetDto.getName());
                    budget.setAmount(budgetDto.getAmount());
                    budget.setCategory(budgetDto.getCategory());
                    budget.setStartDate(budgetDto.getStartDate());
                    budget.setEndDate(budgetDto.getEndDate());
                    return ResponseEntity.ok(convertToDto(budgetRepository.save(budget)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return budgetRepository.findById(id)
                .<ResponseEntity<Void>>map(budget -> {
                    if (!budget.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.notFound().build();
                    }
                    budgetRepository.delete(budget);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 