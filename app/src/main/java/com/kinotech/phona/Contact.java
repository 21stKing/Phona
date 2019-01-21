package com.kinotech.phona;

public class Contact {

    private int contact_id;
    private String contact_name;
    private String contact_value;
    private String contact_description;

    public Contact(int id, String name, String description, String value )
    {
        this.contact_id = id;
        this.contact_name = name;
        this.contact_description = description;
        this.contact_value = value;
    }

    public Contact()
    {

    }

    protected void setContactID(int id){this.contact_id = id;	}
    protected void setContactName(String name){this.contact_name = name;	}
    protected void setContactDescription(String description){this.contact_description = description;	}
    protected void setContactValue(String value){this.contact_value = value;	}

    protected int getContactID(){return this.contact_id;}
    protected String getContactName(){return this.contact_name;}
    protected String getContactDescription(){return this.contact_description;}
    protected String getContactValue(){return this.contact_value;}

}
