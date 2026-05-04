package com.leavemanagement.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "identity-service", path = "/api/auth")
public interface IdentityClient {

    @GetMapping("/internal/users/{employeeCode}")
    ResponseEntity<Map<String, Object>> getUserSummary(@PathVariable("employeeCode") String employeeCode);
}
