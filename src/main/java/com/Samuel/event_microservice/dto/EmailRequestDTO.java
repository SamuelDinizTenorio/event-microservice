package com.Samuel.event_microservice.dto;

public record EmailRequestDTO(
        String to,
        String subject,
        String body
) {
}
