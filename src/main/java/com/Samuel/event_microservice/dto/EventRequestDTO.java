package com.Samuel.event_microservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EventRequestDTO(
        @Min(value = 1, message = "O número máximo de participantes deve ser pelo menos 1.")
        int maxParticipants,

        @NotBlank(message = "O título não pode estar em branco.")
        String title,

        @NotNull(message = "A data não pode ser nula.")
        @Future(message = "A data do evento deve ser no futuro.")
        LocalDate date,

        @NotBlank(message = "A descrição não pode estar em branco.")
        String description
) {
}
