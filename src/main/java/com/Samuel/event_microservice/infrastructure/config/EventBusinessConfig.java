package com.Samuel.event_microservice.infrastructure.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Carrega as propriedades de configuração relacionadas às regras de negócio de eventos.
 * Mapeia as propriedades sob o prefixo 'app.business.event'.
 */
@Configuration
@ConfigurationProperties(prefix = "app.business.event")
@Getter
@Setter
@Validated // Habilita a validação dos campos desta classe de configuração
public class EventBusinessConfig {

    /**
     * A duração mínima que um evento deve ter, em minutos.
     * O valor é carregado da propriedade 'app.business.event.min-duration-minutes'.
     */
    @Min(1) // Garante que o valor configurado seja pelo menos 1
    private int minDurationMinutes;

}
