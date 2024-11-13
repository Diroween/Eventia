package com.proyect;

public class EventRequest
{
    private String eventId;
    private String status;

    public EventRequest() {}

    public EventRequest(String eventId, String status)
    {
        this.eventId = eventId;
        this.status = status;
    }

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

