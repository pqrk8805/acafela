package com.acafela.harmony.ui.contacts;

public class ContactEntry
{
    public long id;
    public String name;
    public String phone;
    public String email;

    public ContactEntry(String id, String name, String phone, String email)
    {
        this.id = Long.parseLong(id);
        this.name = name;
        this.phone = phone;
        this.email = email;
    }
}
