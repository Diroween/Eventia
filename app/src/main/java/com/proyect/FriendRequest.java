package com.proyect;

/**
 * Clase que representa una petición de amistad para la base de datos
 * */

public class FriendRequest
{
    /**
     * variables de clase para saber de quien es la petición y su status
     * */
    private String from;
    private String status;
    private String name;

    /**
     * Constructor vacío necesario para la base datos
     * */
    public FriendRequest()
    {
        //Constructor vacío necesario
    }

    /**
     * Constructor con argumentos
     * */
    public FriendRequest(String from, String name, String status)
    {
        this.name = name;
        this.from = from;
        this.status = status;
    }

    /**
     * Métodos getters and setters
     * */
    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setDisplayName(String displayName) {
        this.name = displayName;
    }
}
