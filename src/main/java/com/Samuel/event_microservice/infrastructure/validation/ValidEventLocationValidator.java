package com.Samuel.event_microservice.infrastructure.validation;

import com.Samuel.event_microservice.core.validation.ValidEventLocation;
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.validation.utils.ValidationUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementa a lógica de validação para a anotação {@link ValidEventLocation}.
 * <p>
 * Esta classe verifica se a localização de um {@link EventRequestDTO} é válida
 * com base no fato de o evento ser remoto ou presencial.
 */
public class ValidEventLocationValidator implements ConstraintValidator<ValidEventLocation, EventRequestDTO> {

    @Override
    public void initialize(ValidEventLocation constraintAnnotation) {
        // Nenhum estado precisa ser inicializado a partir da anotação.
    }

    /**
     * Valida o {@link EventRequestDTO} fornecido com base nas regras de localização.
     *
     * @param event   O objeto DTO do evento a ser validado.
     * @param context O contexto no qual a restrição é avaliada.
     * @return {@code true} se a localização for válida, {@code false} caso contrário.
     */
    @Override
    public boolean isValid(EventRequestDTO event, ConstraintValidatorContext context) {
        if (event == null) {
            return true; // A validação de nulidade do objeto principal é tratada por @NotNull.
        }

        boolean isRemote = event.isRemote();
        boolean hasEventUrl = event.eventUrl() != null && !event.eventUrl().isBlank();
        boolean hasLocation = event.location() != null && !event.location().isBlank();

        // Validação para eventos remotos
        if (isRemote) {
            if (!hasEventUrl) {
                ValidationUtils.reportViolation(context, "A URL do evento é obrigatória para eventos remotos.", "eventUrl");
                return false;
            }
            if (hasLocation) {
                ValidationUtils.reportViolation(context, "A localização deve estar em branco para eventos remotos.", "location");
                return false;
            }
        }

        // Validação para eventos presenciais
        if (!isRemote) {
            if (!hasLocation) {
                ValidationUtils.reportViolation(context, "A localização é obrigatória para eventos presenciais.", "location");
                return false;
            }
            if (hasEventUrl) {
                ValidationUtils.reportViolation(context, "A URL do evento deve estar em branco para eventos presenciais.", "eventUrl");
                return false;
            }
        }

        // Se nenhuma das condições de erro acima foi atendida, o DTO é válido.
        return true;
    }
}
