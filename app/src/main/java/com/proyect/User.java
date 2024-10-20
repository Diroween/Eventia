package com.proyect;

/**
 * Clase usuario, que representa los datos de un usuario en la app
 * Estos datos son los que se mostrarán a otros usuarios de la app
 * */

public class User
{
    private String email;
    private String displayName;

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

    public User(String displayName, String email)
    {
        this.email = email;
        this.displayName = displayName;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
