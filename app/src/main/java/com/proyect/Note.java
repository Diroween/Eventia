package com.proyect;

public class Note
{
    String noteName;
    String noteBody;

    public Note(String noteName, String noteBody)
    {
        this.noteBody = noteBody;
        this.noteName = noteName;
    }

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
