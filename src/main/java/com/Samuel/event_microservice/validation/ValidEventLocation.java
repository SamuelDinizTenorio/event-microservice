package com.Samuel.event_microservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação de validação para garantir que a localização de um evento seja consistente com seu tipo (remoto ou presencial).
 * <p>
 * A validação é aplicada no nível da classe e verifica as seguintes regras:
 * <ul>
 *     <li>Se o evento for remoto ({@code isRemote = true}), o campo {@code eventUrl} deve ser preenchido e o campo {@code location} deve ser nulo ou vazio.</li>
 *     <li>Se o evento for presencial ({@code isRemote = false}), o campo {@code location} deve ser preenchido e o campo {@code eventUrl} deve ser nulo ou vazio.</li>
 * </ul>
 * A lógica de validação é implementada em {@link ValidEventLocationValidator}.
 */
@Constraint(validatedBy = ValidEventLocationValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEventLocation {

    /**
     * @return A mensagem de erro a ser exibida se a validação falhar.
     */
    String message() default "A localização do evento é inválida para o tipo de evento selecionado.";

    /**
     * @return Os grupos de validação aos quais esta restrição pertence.
     */
    Class<?>[] groups() default {};

    /**
     * @return A carga útil (payload) que pode ser associada à restrição.
     */
    Class<? extends Payload>[] payload() default {};
}
