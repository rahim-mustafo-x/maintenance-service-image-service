package org.safa.maintenanceservice.feignClient;

import org.safa.maintenanceservice.model.dto.ApiResponse;
import org.safa.maintenanceservice.model.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("user-service")
public interface UserFeignClient {
    @GetMapping("/v1/user/me")
    ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken);
}
