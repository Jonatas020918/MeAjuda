package com.resourcetech.meajuda.presentation.controllers;

import com.resourcetech.meajuda.application.dtos.FinancialGoalDto;
import com.resourcetech.meajuda.domain.entities.FinancialGoal;
import com.resourcetech.meajuda.domain.entities.GoalStatus;
import com.resourcetech.meajuda.domain.entities.User;
import com.resourcetech.meajuda.domain.repositories.FinancialGoalRepository;
import com.resourcetech.meajuda.domain.repositories.UserRepository;
import com.resourcetech.meajuda.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class FinancialGoalController {

    private final FinancialGoalRepository goalRepository;
    private final UserRepository userRepository;

    public FinancialGoalController(
            FinancialGoalRepository goalRepository,
            UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    private FinancialGoalDto convertToDto(FinancialGoal goal) {
        var dto = new FinancialGoalDto();
        dto.setId(goal.getId());
        dto.setName(goal.getName());
        dto.setDescription(goal.getDescription());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setCurrentAmount(goal.getCurrentAmount());
        dto.setTargetDate(goal.getTargetDate());
        dto.setStatus(goal.getStatus());
        return dto;
    }

    private FinancialGoal convertToEntity(FinancialGoalDto dto, User user) {
        var goal = new FinancialGoal();
        goal.setId(dto.getId());
        goal.setName(dto.getName());
        goal.setDescription(dto.getDescription());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setCurrentAmount(dto.getCurrentAmount());
        goal.setTargetDate(dto.getTargetDate());
        goal.setStatus(dto.getStatus() != null ? dto.getStatus() : GoalStatus.IN_PROGRESS);
        goal.setUser(user);
        return goal;
    }

    @GetMapping
    public List<FinancialGoalDto> getGoals(@AuthenticationPrincipal UserPrincipal currentUser) {
        return goalRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @GetMapping("/by-status")
    public List<FinancialGoalDto> getGoalsByStatus(
            @RequestParam GoalStatus status,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return goalRepository.findByUserIdAndStatus(currentUser.getId(), status)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<FinancialGoalDto> createGoal(
            @Valid @RequestBody FinancialGoalDto goalDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        var user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var goal = convertToEntity(goalDto, user);
        var savedGoal = goalRepository.save(goal);
        return ResponseEntity.ok(convertToDto(savedGoal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialGoalDto> getGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return goalRepository.findById(id)
                .<ResponseEntity<FinancialGoalDto>>map(goal -> {
                    if (!goal.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok(convertToDto(goal));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinancialGoalDto> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody FinancialGoalDto goalDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return goalRepository.findById(id)
                .<ResponseEntity<FinancialGoalDto>>map(goal -> {
                    if (!goal.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.notFound().build();
                    }

                    var user = userRepository.findById(currentUser.getId())
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    var updatedGoal = convertToEntity(goalDto, user);
                    updatedGoal.setId(id);
                    return ResponseEntity.ok(convertToDto(goalRepository.save(updatedGoal)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FinancialGoalDto> updateGoalStatus(
            @PathVariable Long id,
            @RequestParam GoalStatus status,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return goalRepository.findById(id)
                .<ResponseEntity<FinancialGoalDto>>map(goal -> {
                    if (!goal.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.notFound().build();
                    }
                    goal.setStatus(status);
                    return ResponseEntity.ok(convertToDto(goalRepository.save(goal)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return goalRepository.findById(id)
                .<ResponseEntity<Void>>map(goal -> {
                    if (!goal.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.notFound().build();
                    }
                    goalRepository.delete(goal);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 