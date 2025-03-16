package com.resourcetech.meajuda.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourcetech.meajuda.application.dtos.ExpenseDto;
import com.resourcetech.meajuda.domain.entities.Budget;
import com.resourcetech.meajuda.domain.entities.Expense;
import com.resourcetech.meajuda.domain.entities.User;
import com.resourcetech.meajuda.domain.repositories.BudgetRepository;
import com.resourcetech.meajuda.domain.repositories.ExpenseRepository;
import com.resourcetech.meajuda.domain.repositories.UserRepository;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseRepository expenseRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BudgetRepository budgetRepository;

    private User testUser;
    private Budget testBudget;
    private Expense testExpense;
    private ExpenseDto testExpenseDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setName("Monthly Budget");
        testBudget.setUser(testUser);

        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setDescription("Test Expense");
        testExpense.setAmount(new BigDecimal("50.00"));
        testExpense.setExpenseDate(LocalDateTime.now());
        testExpense.setCategory("Food");
        testExpense.setUser(testUser);
        testExpense.setBudget(testBudget);

        testExpenseDto = new ExpenseDto();
        testExpenseDto.setDescription("Test Expense");
        testExpenseDto.setAmount(new BigDecimal("50.00"));
        testExpenseDto.setExpenseDate(LocalDateTime.now());
        testExpenseDto.setCategory("Food");
        testExpenseDto.setBudgetId(1L);
    }

    @Test
    @WithMockUser
    public void testGetExpenses() throws Exception {
        when(expenseRepository.findByUserId(any())).thenReturn(Arrays.asList(testExpense));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value(testExpense.getDescription()));
    }

    @Test
    @WithMockUser
    public void testGetExpensesByDateRange() throws Exception {
        when(expenseRepository.findByUserIdAndExpenseDateBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testExpense));

        mockMvc.perform(get("/api/expenses/by-date-range")
                .param("startDate", LocalDateTime.now().minusDays(7).toString())
                .param("endDate", LocalDateTime.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value(testExpense.getDescription()));
    }

    @Test
    @WithMockUser
    public void testGetExpensesByCategory() throws Exception {
        when(expenseRepository.findByUserIdAndCategory(any(), any()))
                .thenReturn(Arrays.asList(testExpense));

        mockMvc.perform(get("/api/expenses/by-category")
                .param("category", "Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value(testExpense.getDescription()));
    }

    @Test
    @WithMockUser
    public void testCreateExpense() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(any())).thenReturn(Optional.of(testBudget));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testExpenseDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(testExpense.getDescription()));
    }

    @Test
    @WithMockUser
    public void testGetExpense() throws Exception {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        mockMvc.perform(get("/api/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(testExpense.getDescription()));
    }

    @Test
    @WithMockUser
    public void testUpdateExpense() throws Exception {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(any())).thenReturn(Optional.of(testBudget));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        mockMvc.perform(put("/api/expenses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testExpenseDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(testExpense.getDescription()));
    }

    @Test
    @WithMockUser
    public void testDeleteExpense() throws Exception {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testExpenseNotFound() throws Exception {
        when(expenseRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/expenses/1"))
                .andExpect(status().isNotFound());
    }
} 