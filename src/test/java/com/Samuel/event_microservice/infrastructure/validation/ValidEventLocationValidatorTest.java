package com.Samuel.event_microservice.infrastructure.validation;

import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.validation.utils.ValidationUtils;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class ValidEventLocationValidatorTest {

    @InjectMocks
    private ValidEventLocationValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    // Método auxiliar para criar DTOs com valores padrão
    private static EventRequestDTO createDto(String eventUrl, String location, boolean isRemote) {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        return new EventRequestDTO("Title", "Desc", start, start.plusHours(2), 100, "img.url", eventUrl, location, isRemote);
    }

    @Nested
    @DisplayName("Valid Scenarios")
    class ValidScenarios {

        // Fonte de argumentos para os cenários válidos
        static Stream<Arguments> validEventProvider() {
            return Stream.of(
                    arguments(createDto("http://event.url", null, true)),  // Cenário 1: Remoto e válido
                    arguments(createDto(null, "Local do Evento", false)) // Cenário 2: Presencial e válido
            );
        }

        @ParameterizedTest
        @MethodSource("validEventProvider")
        @DisplayName("Should return TRUE for valid combinations")
        void isValid_whenCombinationIsValid_shouldReturnTrue(EventRequestDTO validDto) {
            // Act
            boolean result = validator.isValid(validDto, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return TRUE for a null DTO")
        void isValid_whenDtoIsNull_shouldReturnTrue() {
            // O teste para DTO nulo é mantido separado porque é um caso de borda distinto
            // e não se encaixa no padrão de "combinação válida".
            assertThat(validator.isValid(null, context)).isTrue();
        }
    }

    @Nested
    @DisplayName("Invalid Scenarios")
    class InvalidScenarios {

        // Fonte de argumentos para o teste parametrizado
        static Stream<Arguments> invalidEventProvider() {
            return Stream.of(
                    arguments(createDto(" ", null, true), "A URL do evento é obrigatória para eventos remotos.", "eventUrl"),
                    arguments(createDto("http://event.url", "Algum Local", true), "A localização deve estar em branco para eventos remotos.", "location"),
                    arguments(createDto(null, " ", false), "A localização é obrigatória para eventos presenciais.", "location"),
                    arguments(createDto("http://event.url", "Local do Evento", false), "A URL do evento deve estar em branco para eventos presenciais.", "eventUrl")
            );
        }

        @ParameterizedTest
        @MethodSource("invalidEventProvider")
        @DisplayName("Should return FALSE and report violation for invalid combinations")
        void isValid_whenCombinationIsInvalid_shouldReturnFalse(EventRequestDTO dto, String expectedMessage, String expectedField) {
            try (MockedStatic<ValidationUtils> mockedUtils = Mockito.mockStatic(ValidationUtils.class)) {
                // Act
                boolean result = validator.isValid(dto, context);

                // Assert
                assertThat(result).isFalse();
                mockedUtils.verify(() -> ValidationUtils.reportViolation(context, expectedMessage, expectedField));
            }
        }
    }
}