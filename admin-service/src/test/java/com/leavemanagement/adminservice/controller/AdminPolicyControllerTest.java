package com.leavemanagement.adminservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leavemanagement.adminservice.dto.LeavePolicyDto;
import com.leavemanagement.adminservice.service.AdminPolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPolicyController.class)
public class AdminPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminPolicyService adminPolicyService;

    @Test
    void testGetAllPolicies() throws Exception {
        LeavePolicyDto dto = new LeavePolicyDto();
        dto.setPolicyCode("TEST");
        when(adminPolicyService.getAllPolicies()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/admin/master/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].policyCode").value("TEST"));
    }

    @Test
    void testGetPolicyByCode() throws Exception {
        LeavePolicyDto dto = new LeavePolicyDto();
        dto.setPolicyCode("TEST");
        when(adminPolicyService.getPolicyByCode("TEST")).thenReturn(dto);

        mockMvc.perform(get("/api/admin/master/policies/TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyCode").value("TEST"));
    }

    @Test
    void testCreateOrUpdatePolicy() throws Exception {
        LeavePolicyDto dto = new LeavePolicyDto();
        dto.setPolicyCode("TEST");
        dto.setLeaveType("Testing");
        dto.setAnnualAllocation(10.0);
        when(adminPolicyService.createOrUpdatePolicy(any(LeavePolicyDto.class))).thenReturn(dto);

        mockMvc.perform(post("/api/admin/master/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyCode").value("TEST"));
    }

    @Test
    void testDeletePolicy() throws Exception {
        mockMvc.perform(delete("/api/admin/master/policies/TEST"))
                .andExpect(status().isNoContent());
    }
}
