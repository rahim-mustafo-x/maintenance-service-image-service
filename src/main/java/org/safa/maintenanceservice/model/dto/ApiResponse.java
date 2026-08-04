package org.safa.maintenanceservice.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ApiResponse <T> {
    private int code;
    private T data;
    private String message;
}
