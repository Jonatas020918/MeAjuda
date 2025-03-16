package com.resourcetech.meajuda.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourcetech.meajuda.application.dtos.BudgetDto;
import com.resourcetech.meajuda.domain.entities.Budget;
import com.resourcetech.meajuda.domain.entities.User;
import com.resourcetech.meajuda.domain.repositories.BudgetRepository;
import com.resourcetech.meajuda.domain.repositories.UserRepository;
import com.resourcetech.meajuda.infrastructure.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BudgetRepository budgetRepository;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private Budget testBudget;
    private BudgetDto testBudgetDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setName("Monthly Budget");
        testBudget.setAmount(new BigDecimal("1000.00"));
        testBudget.setCategory("General");
        testBudget.setStartDate(LocalDate.now());
        testBudget.setEndDate(LocalDate.now().plusMonths(1));
        testBudget.setUser(testUser);

        testBudgetDto = new BudgetDto();
        testBudgetDto.setName("Monthly Budget");
        testBudgetDto.setAmount(new BigDecimal("1000.00"));
        testBudgetDto.setCategory("General");
        testBudgetDto.setStartDate(LocalDate.now());
        testBudgetDto.setEndDate(LocalDate.now().plusMonths(1));
    }

    @Test
    @WithMockUser
    public void testGetBudgets() throws Exception {
        when(budgetRepository.findByUserId(any())).thenReturn(Arrays.asList(testBudget));

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(testBudget.getName()))
                .andExpect(jsonPath("$[0].amount").value(testBudget.getAmount().doubleValue()));
    }

    @Test
    @WithMockUser
    public void testCreateBudget() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBudgetDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testBudget.getName()));
    }

    @Test
    @WithMockUser
    public void testGetBudget() throws Exception {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        mockMvc.perform(get("/api/budgets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testBudget.getName()));
    }

    @Test
    @WithMockUser
    public void testGetBudgetNotFound() throws Exception {
        when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/budgets/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testUpdateBudget() throws Exception {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        mockMvc.perform(put("/api/budgets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBudgetDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testBudget.getName()));
    }

    @Test
    @WithMockUser
    public void testDeleteBudget() throws Exception {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        mockMvc.perform(delete("/api/budgets/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testUpdateBudgetNotFound() throws Exception {
        when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/budgets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBudgetDto)))
                .andExpect(status().isNotFound());
    }
} 