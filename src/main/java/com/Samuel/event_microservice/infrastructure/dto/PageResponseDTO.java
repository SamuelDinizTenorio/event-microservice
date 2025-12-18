package com.Samuel.event_microservice.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Um DTO genérico para representar uma resposta paginada de forma estável.
 * <p>
 * Este DTO desacopla a estrutura da API da implementação interna do Spring Data (Page/PageImpl),
 * garantindo um contrato de API consistente e robusto.
 *
 * @param <T> O tipo do conteúdo da página.
 * @param content A lista de itens na página atual.
 * @param page O número da página atual (baseado em zero).
 * @param size O tamanho da página.
 * @param totalElements O número total de elementos em todas as páginas.
 * @param totalPages O número total de páginas.
 * @param isLast Indica se esta é a última página.
 */
public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("is_last") boolean isLast
) {
    /**
     * Construtor de conveniência para criar um PageResponseDTO a partir de um objeto Page do Spring Data.
     *
     * @param page O objeto Page retornado pelo repositório.
     */
    public PageResponseDTO(Page<T> page) {
        this(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
