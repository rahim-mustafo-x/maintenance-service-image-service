package org.safa.maintenanceservice.model.dto;

public record ImageByteResponse(
        byte[] data,
        String contentType
) {}
