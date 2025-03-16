package com.resourcetech.meajuda.presentation.controllers;

import com.resourcetech.meajuda.application.dtos.ExpenseDto;
import com.resourcetech.meajuda.domain.entities.Budget;
import com.resourcetech.meajuda.domain.entities.Expense;
import com.resourcetech.meajuda.domain.entities.User;
import com.resourcetech.meajuda.domain.repositories.BudgetRepository;
import com.resourcetech.meajuda.domain.repositories.ExpenseRepository;
import com.resourcetech.meajuda.domain.repositories.UserRepository;
import com.resourcetech.meajuda.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    public ExpenseController(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
    }

    private ExpenseDto convertToDto(Expense expense) {
        var dto = new ExpenseDto();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setCategory(expense.getCategory());
        dto.setBudgetId(expense.getBudget() != null ? expense.getBudget().getId() : null);
        dto.setPaymentMethod(expense.getPaymentMethod());
        dto.setRecurring(expense.isRecurring());
        dto.setRecurrencePeriod(expense.getRecurrencePeriod());
        return dto;
    }

    private Expense convertToEntity(ExpenseDto dto, User user) {
        var expense = new Expense();
        expense.setId(dto.getId());
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setCategory(dto.getCategory());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setRecurring(dto.isRecurring());
        expense.setRecurrencePeriod(dto.getRecurrencePeriod());
        expense.setUser(user);

        if (dto.getBudgetId() != null) {
            Budget budget = budgetRepository.findById(dto.getBudgetId())
                    .orElseThrow(() -> new RuntimeException("Budget not found"));
            expense.setBudget(budget);
        }

        return expense;
    }

    @GetMapping
    public List<ExpenseDto> getExpenses(@AuthenticationPrincipal UserPrincipal currentUser) {
        return expenseRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @GetMapping("/by-date-range")
    public List<ExpenseDto> getExpensesByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return expenseRepository.findByUserIdAndExpenseDateBetween(currentUser.getId(), startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @GetMapping("/by-category")
    public List<ExpenseDto> getExpensesByCategory(
            @RequestParam String category,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return expenseRepository.findByUserIdAndCategory(currentUser.getId(), category)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(
            @Valid @RequestBody ExpenseDto expenseDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        var user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var expense = convertToEntity(expenseDto, user);
        var savedExpense = expenseRepository.save(expense);
        return ResponseEntity.ok(convertToDto(savedExpense));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return expenseRepository.findById(id)
                .<ResponseEntity<ExpenseDto>>map(expense -> {
                    if (!expense.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(convertToDto(expense));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDto expenseDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return expenseRepository.findById(id)
                .<ResponseEntity<ExpenseDto>>map(expense -> {
                    if (!expense.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.notFound().build();
                    }
                    
                    var user = userRepository.findById(currentUser.getId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    
                    var updatedExpense = convertToEntity(expenseDto, user);
                    updatedExpense.setId(id);
                    return ResponseEntity.ok(convertToDto(expenseRepository.save(updatedExpense)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return expenseRepository.findById(id)
                .<ResponseEntity<Void>>map(expense -> {
                    if (!expense.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.notFound().build();
                    }
                    expenseRepository.delete(expense);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 