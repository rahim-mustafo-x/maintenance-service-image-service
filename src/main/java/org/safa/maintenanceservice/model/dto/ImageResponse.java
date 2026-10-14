package org.safa.maintenanceservice.model.dto;

import org.safa.maintenanceservice.model.ImageType;

import java.util.UUID;

public record ImageResponse(
        UUID id,
        String contentType,
        long ownerId,
        ImageType imageType
) {}