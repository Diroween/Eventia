package com.proyect;

/**
 * Clase que representa una invitación de evento
 * */

public class EventRequest
{
    private String eventId;
    private String status;

    /**
     * Constructor vacío necesario para la serialización
     * */

    public EventRequest() {}

    /**
     * Constructor con argumentos
     * */
    public EventRequest(String eventId, String status)
    {
        this.eventId = eventId;
        this.status = status;
    }

    /**
     * Métodos getter and setter
     * */
    public String getEventId()
    {
        return eventId;
    }

    public void setEventId(String eventId)
    {
        this.eventId = eventId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}

