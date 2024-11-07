package com.proyect;

/**
 * Clase Event que representa un event en la aplicación
 * */

public class Event
{
    /**
     * Creamos los atributos que definen a la clase Event
     * */

    private String id;
    private String name;
    private String date;
    private String place;
    private String image;
    private String hour;

    /**
     * Cremos un constructor vacío para poder realizar la serialización de los datos
     * */

    public Event()
    {
        //Constructor vacío necesario para la serialización de los datos
    }

    /**
     * Creamos un constructor con argumentos, tantos como atributos tiene
     * */

    public Event(String id, String name, String date, String place, String image, String hour) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.place = place;
        this.image = image;
        this.hour = hour;
    }

    /**
     * Creamos un constructor con argumentos, sin la hora
     * */

    public Event(String id, String name, String date, String place, String image)
    {
        this.id = id;
        this.name = name;
        this.date = date;
        this.place = place;
        this.image = image;
    }

    /**
     * Métodos getter and setter
     * */

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getPlace()
    {
        return place;
    }

    public void setPlace(String place)
    {
        this.place = place;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getHour()
    {
        return hour;
    }

    public void setHour(String hour)
    {
        this.hour = hour;
    }
}
