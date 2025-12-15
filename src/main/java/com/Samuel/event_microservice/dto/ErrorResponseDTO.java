package com.Samuel.event_microservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para padronizar as respostas de erro da API, com suporte detalhado para erros de validação.
 *
 * @param status    O código de status HTTP (ex: 400, 404).
 * @param error     A descrição do status HTTP (ex: "Bad Request", "Not Found").
 * @param message   Uma mensagem geral descrevendo o erro principal.
 * @param path      O caminho do endpoint que originou o erro.
 * @param timestamp A data e hora em que o erro ocorreu.
 * @param errors    Um mapa contendo os erros de validação específicos (campo -> mensagem de erro).
 *                  Este campo só aparece na resposta JSON se houver erros de validação.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> errors
) {
    // Construtor para erros gerais (sem detalhes de validação)
    public ErrorResponseDTO(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now(), null);
    }

    // Construtor para erros de validação
    public ErrorResponseDTO(int status, String error, String message, String path, Map<String, String> errors) {
        this(status, error, message, path, LocalDateTime.now(), errors);
    }
}
