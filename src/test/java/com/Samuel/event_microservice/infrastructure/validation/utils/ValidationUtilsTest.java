package com.Samuel.event_microservice.infrastructure.validation.utils;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationUtilsTest {

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @Test
    @DisplayName("Should correctly build and report a constraint violation")
    void reportViolation_shouldBuildAndReportViolation() {
        // Arrange
        String testMessage = "This is a test message";
        String testProperty = "testProperty";

        // Configura o comportamento encadeado dos mocks
        when(context.buildConstraintViolationWithTemplate(testMessage))
                .thenReturn(builder);
        when(builder.addPropertyNode(testProperty))
                .thenReturn(nodeBuilder);

        // Act
        ValidationUtils.reportViolation(context, testMessage, testProperty);

        // Assert
        // Verifica se os métodos foram chamados na ordem correta e com os argumentos corretos
        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(context, times(1)).buildConstraintViolationWithTemplate(testMessage);
        verify(builder, times(1)).addPropertyNode(testProperty);
        verify(nodeBuilder, times(1)).addConstraintViolation();
    }
}
