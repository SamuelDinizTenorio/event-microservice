package com.Samuel.event_microservice.test_support;

import jakarta.validation.constraints.NotBlank;

public record ValidationTestDTO(@NotBlank(message = "O nome não pode estar em branco.") String name) { }
