package com.Samuel.event_microservice.infrastructure.exceptions;

import com.Samuel.event_microservice.core.usecases.EventUseCase;
import com.Samuel.event_microservice.infrastructure.exceptions.helper.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test") // Separa configurações de teste
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventUseCase eventUseCase;

    @BeforeEach
    void setUp() {
        // Configura o MockMvc em modo standalone com um Controller de teste
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 400 Bad Request for MethodArgumentNotValidException")
    void handleValidationExceptions() throws Exception {
        // Arrange
        String requestBody = "{\"name\":\"\"}"; // 'name' está em branco, violando @NotBlank

        // Act & Assert
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("A validação falhou para um ou mais campos."))
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.timestamp").exists())
                // Verifica se o mapa de erros contém o erro de validação específico
                .andExpect(jsonPath("$.errors.name").value("O nome não pode estar em branco."));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for HttpMessageNotReadableException")
    void handleHttpMessageNotReadableException() throws Exception {
        // Arrange
        String expectedMessage = "O corpo da requisição está ausente ou malformado.";
        String expectedPath = "/test/message-not-readable";

        // Act & Assert
        // Envia uma requisição POST sem corpo para acionar a exceção
        mockMvc.perform(post(expectedPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(expectedPath))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return 404 Not Found for EventNotFoundException")
    void handleEventNotFoundException() throws Exception {
        // Arrange
        String expectedMessage = "Evento de teste não encontrado.";
        String expectedPath = "/test/event-not-found";

        // Act & Assert
        mockMvc.perform(get(expectedPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(expectedPath))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @DisplayName("Should return 409 Conflict for EventFullException")
    void handleEventFullException() throws Exception {
        // Arrange
        String expectedMessage = "Evento de teste está lotado.";
        String expectedPath = "/test/event-full";

        // Act & Assert
        mockMvc.perform(get(expectedPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(expectedPath))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @DisplayName("Should return 409 Conflict for SubscriptionAlreadyExistsException")
    void handleSubscriptionAlreadyExistsException() throws Exception {
        // Arrange
        String expectedMessage = "Inscrição de teste já existe.";
        String expectedPath = "/test/subscription-exists";

        // Act & Assert
        mockMvc.perform(get(expectedPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(expectedPath))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @DisplayName("Should return 400 Bad Request for IllegalArgumentException")
    void handleIllegalArgumentException() throws Exception {
        // Arrange
        String expectedMessage = "Argumento de teste inválido.";
        String expectedPath = "/test/illegal-argument";

        // Act & Assert
        mockMvc.perform(get(expectedPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(expectedPath))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @DisplayName("Should return 400 Bad Request for IllegalStateException")
    void handleIllegalStateException() throws Exception {
        // Arrange
        String expectedMessage = "O estado do recurso é inválido para esta operação.";
        String expectedPath = "/test/illegal-state";

        // Act & Assert
        mockMvc.perform(get(expectedPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(expectedPath))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return 415 Unsupported Media Type for HttpMediaTypeNotSupportedException")
    void handleHttpMediaTypeNotSupportedException() throws Exception {
        // Arrange
        String requestBody = "{\"name\":\"Teste\"}";
        String expectedPath = "/test/validation"; // Endpoint que espera JSON

        // Act & Assert
        mockMvc.perform(post(expectedPath)
                        .contentType(MediaType.APPLICATION_XML) // Envia um tipo de mídia não suportado
                        .content(requestBody))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.message").value("Unsupported Media Type. Please use application/json."))
                .andExpect(jsonPath("$.path").value(expectedPath))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return 405 Method Not Allowed for HttpRequestMethodNotSupportedException")
    void handleHttpRequestMethodNotSupportedException() throws Exception {
        // Arrange
        String expectedPath = "/test/validation"; // Endpoint que existe, mas só para POST

        // Act & Assert
        mockMvc.perform(get(expectedPath)) // Tenta usar GET em um endpoint que só aceita POST
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.message").value("Method Not Allowed. Supported methods: [POST]"))
                .andExpect(jsonPath("$.path").value(expectedPath))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return 404 Not Found for NoHandlerFoundException")
    void handleNoHandlerFoundException() throws Exception {
        // Arrange
        String nonExistentPath = "/non-existent-endpoint";

        // Act & Assert
        mockMvc.perform(get(nonExistentPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("O endpoint '" + nonExistentPath + "' não foi encontrado."))
                .andExpect(jsonPath("$.path").value(nonExistentPath))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error for any unhandled exception")
    void handleGlobalException() throws Exception {
        // Arrange
        String expectedPath = "/test/unhandled-exception";
        String genericError = "Ocorreu um erro inesperado no servidor.";

        // Act & Assert
        mockMvc.perform(get(expectedPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value(genericError))
                .andExpect(jsonPath("$.path").value(expectedPath))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }
}
