package com.proyect.note;

/**
 * Clase que representa una nota en la app
 * */

public class Note
{
    /**
     * Variables de clase, el titutlo de la nota y su cuerpo
     * */

    private String noteName;
    private String noteBody;

    /**
     * Constructor con argumentos
     * */

    public Note(String noteName, String noteBody)
    {
        this.noteBody = noteBody;
        this.noteName = noteName;
    }

    /**
     * Getters and Setters
     * */

    public String getNoteName()
    {
        return noteName;
    }

    public void setNoteName(String noteName)
    {
        this.noteName = noteName;
    }

    public String getNoteBody()
    {
        return noteBody;
    }

    public void setNoteBody(String noteBody)
    {
        this.noteBody = noteBody;
    }
}
