package com.Samuel.event_microservice.validation.utils;

import jakarta.validation.ConstraintValidatorContext;

/**
 * Classe utilitária para ajudar com operações de validação.
 */
public final class ValidationUtils {

    /**
     * Construtor privado para impedir a instanciação da classe utilitária.
     */
    private ValidationUtils() {}

    /**
     * Reporta uma violação de restrição em um campo específico.
     *
     * @param context      O contexto da validação.
     * @param message      A mensagem de erro específica.
     * @param propertyName O nome da propriedade (campo) que causou a violação.
     */
    public static void reportViolation(ConstraintValidatorContext context, String message, String propertyName) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyName)
                .addConstraintViolation();
    }
}
