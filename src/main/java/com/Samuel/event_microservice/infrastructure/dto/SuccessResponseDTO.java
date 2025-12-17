package com.Samuel.event_microservice.infrastructure.dto;

/**
 * DTO para encapsular uma mensagem de resposta de sucesso genérica.
 *
 * @param message A mensagem de sucesso a ser retornada.
 */
public record SuccessResponseDTO(String message) {
}
