package com.proyect;

public class Friend
{
    private String id;
    private String name;

    public Friend()
    {

    }

    public Friend(String id, String name)
    {
        this.name = name;
        this.id = id;
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
}
