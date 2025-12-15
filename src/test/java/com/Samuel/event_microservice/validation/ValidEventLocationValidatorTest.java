package com.Samuel.event_microservice.validation;

import com.Samuel.event_microservice.dto.EventRequestDTO;
import com.Samuel.event_microservice.validation.utils.ValidationUtils;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ValidEventLocationValidatorTest {

    @InjectMocks
    private ValidEventLocationValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    // Método auxiliar para criar EventRequestDTOs com valores padrão
    private EventRequestDTO createEventRequestDTO(String eventUrl, String location, Boolean isRemote) {
        return new EventRequestDTO(
                "Título do Evento",
                "Descrição do Evento",
                LocalDateTime.now().plusDays(1), // Data no futuro
                100, // Máximo de participantes
                "http://imagem.url/default.jpg", // URL de imagem padrão
                eventUrl,
                location,
                isRemote
        );
    }

    @Test
    @DisplayName("Should return TRUE when event is remote, URL is valid, and location is absent")
    void isValid_whenRemoteAndUrlIsValid_thenReturnsTrue() {
        EventRequestDTO dto = createEventRequestDTO("http://event.url", null, true);
        assertTrue(validator.isValid(dto, context));
    }

    @Test
    @DisplayName("Should return TRUE when event is not remote, location is valid, and URL is absent")
    void isValid_whenNotRemoteAndLocationIsValid_thenReturnsTrue() {
        EventRequestDTO dto = createEventRequestDTO(null, "Local do Evento", false);
        assertTrue(validator.isValid(dto, context));
    }

    @Test
    @DisplayName("Should return FALSE when event is remote and URL is missing")
    void isValid_whenRemoteAndUrlIsMissing_thenReturnsFalse() {
        try (MockedStatic<ValidationUtils> mockedUtils = Mockito.mockStatic(ValidationUtils.class)) {
            EventRequestDTO dto = createEventRequestDTO(" ", null, true); // URL vazia
            assertFalse(validator.isValid(dto, context));
            mockedUtils.verify(() -> ValidationUtils.reportViolation(context, "A URL do evento é obrigatória para eventos remotos.", "eventUrl"));
        }
    }

    @Test
    @DisplayName("Should return FALSE when event is remote and location is present")
    void isValid_whenRemoteAndLocationIsPresent_thenReturnsFalse() {
        try (MockedStatic<ValidationUtils> mockedUtils = Mockito.mockStatic(ValidationUtils.class)) {
            EventRequestDTO dto = createEventRequestDTO("http://event.url", "Algum Local", true); // Local presente
            assertFalse(validator.isValid(dto, context));
            mockedUtils.verify(() -> ValidationUtils.reportViolation(context, "A localização deve estar em branco para eventos remotos.", "location"));
        }
    }

    @Test
    @DisplayName("Should return FALSE when event is not remote and location is missing")
    void isValid_whenNotRemoteAndLocationIsMissing_thenReturnsFalse() {
        try (MockedStatic<ValidationUtils> mockedUtils = Mockito.mockStatic(ValidationUtils.class)) {
            EventRequestDTO dto = createEventRequestDTO(null, " ", false); // Local vazio
            assertFalse(validator.isValid(dto, context));
            mockedUtils.verify(() -> ValidationUtils.reportViolation(context, "A localização é obrigatória para eventos presenciais.", "location"));
        }
    }

    @Test
    @DisplayName("Should return FALSE when event is not remote and URL is present")
    void isValid_whenNotRemoteAndUrlIsPresent_thenReturnsFalse() {
        try (MockedStatic<ValidationUtils> mockedUtils = Mockito.mockStatic(ValidationUtils.class)) {
            EventRequestDTO dto = createEventRequestDTO("http://event.url", "Local do Evento", false); // URL presente
            assertFalse(validator.isValid(dto, context));
            mockedUtils.verify(() -> ValidationUtils.reportViolation(context, "A URL do evento deve estar em branco para eventos presenciais.", "eventUrl"));
        }
    }

    @Test
    @DisplayName("Should return TRUE when DTO is null (nullity validation handled elsewhere)")
    void isValid_whenDtoIsNull_thenReturnsTrue() {
        assertTrue(validator.isValid(null, context));
    }
}
