package org.safa.maintenanceservice.model.dto;

import lombok.*;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse <T> {
    private int code;
    private T data;
    private String message;
}
