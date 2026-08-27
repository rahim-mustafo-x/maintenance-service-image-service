package org.safa.maintenanceservice.model.dto;

import java.util.Set;

public record UserResponse(
        long id,
        String fullName,
        String userName,
        String phoneNumber,
        Set<String> roles
) {}