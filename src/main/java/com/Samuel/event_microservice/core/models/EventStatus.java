package com.Samuel.event_microservice.core.models;

/**
 * Representa os possíveis estados de um evento no sistema.
 */
public enum EventStatus {

    ACTIVE, // O evento está ativo e pode receber inscrições.
    CANCELLED, // O evento foi cancelado e não ocorrerá.
    FINISHED // O evento já ocorreu e foi finalizado.
}
