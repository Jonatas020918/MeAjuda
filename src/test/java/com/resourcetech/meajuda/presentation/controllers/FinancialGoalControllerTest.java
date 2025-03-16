package com.resourcetech.meajuda.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourcetech.meajuda.application.dtos.FinancialGoalDto;
import com.resourcetech.meajuda.domain.entities.FinancialGoal;
import com.resourcetech.meajuda.domain.entities.GoalStatus;
import com.resourcetech.meajuda.domain.entities.User;
import com.resourcetech.meajuda.domain.repositories.FinancialGoalRepository;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FinancialGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FinancialGoalRepository goalRepository;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private FinancialGoal testGoal;
    private FinancialGoalDto testGoalDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testGoal = new FinancialGoal();
        testGoal.setId(1L);
        testGoal.setName("Vacation Fund");
        testGoal.setDescription("Save for summer vacation");
        testGoal.setTargetAmount(new BigDecimal("5000.00"));
        testGoal.setCurrentAmount(new BigDecimal("1000.00"));
        testGoal.setTargetDate(LocalDate.now().plusMonths(6));
        testGoal.setStatus(GoalStatus.IN_PROGRESS);
        testGoal.setUser(testUser);

        testGoalDto = new FinancialGoalDto();
        testGoalDto.setName("Vacation Fund");
        testGoalDto.setDescription("Save for summer vacation");
        testGoalDto.setTargetAmount(new BigDecimal("5000.00"));
        testGoalDto.setCurrentAmount(new BigDecimal("1000.00"));
        testGoalDto.setTargetDate(LocalDate.now().plusMonths(6));
        testGoalDto.setStatus(GoalStatus.IN_PROGRESS);
    }

    @Test
    @WithMockUser
    public void testGetGoals() throws Exception {
        when(goalRepository.findByUserId(any())).thenReturn(Arrays.asList(testGoal));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(testGoal.getName()));
    }

    @Test
    @WithMockUser
    public void testGetGoalsByStatus() throws Exception {
        when(goalRepository.findByUserIdAndStatus(any(), any()))
                .thenReturn(Arrays.asList(testGoal));

        mockMvc.perform(get("/api/goals/by-status")
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(testGoal.getName()));
    }

    @Test
    @WithMockUser
    public void testCreateGoal() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        when(goalRepository.save(any(FinancialGoal.class))).thenReturn(testGoal);

        mockMvc.perform(post("/api/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGoalDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testGoal.getName()));
    }

    @Test
    @WithMockUser
    public void testGetGoal() throws Exception {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testGoal.getName()));
    }

    @Test
    @WithMockUser
    public void testUpdateGoal() throws Exception {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        when(goalRepository.save(any(FinancialGoal.class))).thenReturn(testGoal);

        mockMvc.perform(put("/api/goals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGoalDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testGoal.getName()));
    }

    @Test
    @WithMockUser
    public void testUpdateGoalStatus() throws Exception {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        testGoal.setStatus(GoalStatus.COMPLETED);
        when(goalRepository.save(any(FinancialGoal.class))).thenReturn(testGoal);

        mockMvc.perform(patch("/api/goals/1/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser
    public void testDeleteGoal() throws Exception {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));

        mockMvc.perform(delete("/api/goals/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testGoalNotFound() throws Exception {
        when(goalRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isNotFound());
    }
} 