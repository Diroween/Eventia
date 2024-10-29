package com.proyect;

/**
 * Clase usuario, que representa los datos de un usuario en la app
 * Estos datos son los que se mostrarán a otros usuarios de la app
 * */

public class User
{
    private String email;
    private String name;
    private String id;

    /**
     * Constructor vacío de usuario
     * */

    public User()
    {
        //constructor vacío
    }

    /**
     * Constructor con argumentos de un usuario
     * */

    public User(String name, String email, String id)
    {
        this.email = email;
        this.name = name;
        this.id = id;
    }

    public User(String id, String name)
    {
        this.name = name;
        this.id = id;
    }

    /**
     * Getters y Setters necesarios para que la base de datos acceda a los datos de un usuario
     * */

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}