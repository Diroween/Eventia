package com.proyect;

public class Event
{
    private String id;
    private String name;
    private String date;
    private String place;
    private String image;

    public Event()
    {
        //Constructor vacío necesario para la serialización de los datos
    }

    public Event(String id, String name, String date, String place, String image)
    {
        this.id = id;
        this.name = name;
        this.date = date;
        this.place = place;
        this.image = image;
    }

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
}
