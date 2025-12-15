package com.Samuel.event_microservice.dto;

import com.Samuel.event_microservice.validation.ValidEventLocation;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * DTO para encapsular os dados de requisição para a criação de um novo evento.
 * Contém validações para garantir a integridade dos dados de entrada.
 *
 * @param title           O título do evento. Não pode estar em branco.
 * @param description     A descrição detalhada do evento. Não pode estar em branco.
 * @param date            A data e hora do evento. Não pode ser nula e deve ser no futuro.
 * @param maxParticipants O número máximo de participantes permitidos. Deve ser no mínimo 1.
 * @param imageUrl        A URL de uma imagem de banner para o evento. Deve ser uma URL válida.
 * @param eventUrl        A URL para acessar o evento, caso seja remoto. Validado condicionalmente.
 * @param location        O endereço físico do evento, caso seja presencial. Validado condicionalmente.
 * @param isRemote        Indica se o evento é remoto (online) ou não. Não pode ser nulo.
 */
@ValidEventLocation
public record EventRequestDTO(
        @NotBlank(message = "O título não pode estar em branco.")
        String title,

        @NotBlank(message = "A descrição não pode estar em branco.")
        String description,

        @NotNull(message = "A data não pode ser nula.")
        @Future(message = "A data do evento deve ser no futuro.")
        LocalDateTime date,

        @Min(value = 1, message = "O número máximo de participantes deve ser de no mínimo 1.")
        int maxParticipants,

        @URL(message = "A URL da imagem é inválida.")
        String imageUrl,

        String eventUrl,

        String location,

        @NotNull(message = "É necessário informar se o evento é remoto.")
        Boolean isRemote
) {
}
