package com.Samuel.event_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principal da aplicação Event Microservice.
 * <p>
 * Esta classe inicializa o contexto do Spring Boot, habilita clientes Feign para comunicação
 * com outros microsserviços e ativa o agendamento de tarefas.
 */
@SpringBootApplication
@EnableFeignClients // Habilita a varredura de interfaces @FeignClient
@EnableScheduling // Habilita o suporte a tarefas agendadas do Spring
public class EventMicroserviceApplication {

	/**
	 * Método principal que inicia a aplicação Spring Boot.
	 *
	 * @param args Argumentos de linha de comando passados para a aplicação.
	 */
	public static void main(String[] args) {
		SpringApplication.run(EventMicroserviceApplication.class, args);
	}

}
