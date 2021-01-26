package legal;

/*
 * IMPORTS
 * java.io.Serial: Used for serialisation (saving configuration)
 * java.text.SimpleDateFormat: Used for printing date assigned
 * java.util.Date: Used to hold date assigned
 */

import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * RESPONSIBILITIES
 * - Represents a legal case.
 */

public class LCase implements java.io.Serializable
{
    // Recommended for classes that implement java.io.Serializable
    // Randomly generated
    @Serial
    private static final long serialVersionUID = -7531427557698015639L;

    private String name;
    private LCourt lCourt;
    private LClient lClient;
    private Date dateAssigned;

    public LCase(String name, LCourt lCourt, LClient lClient, Date dateAssigned)
    {
        this.setName(name);
        this.setCourt(lCourt);
        this.setClient(lClient);
        this.setDateAssigned(dateAssigned);
    }

    /* Accessor/mutator methods */

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public LCourt getCourt()
    {
        return this.lCourt;
    }

    public void setCourt(LCourt lCourt)
    {
        this.lCourt = lCourt;
    }

    public LClient getClient()
    {
        return this.lClient;
    }

    public void setClient(LClient lClient)
    {
        this.lClient = lClient;
    }

    public Date getDateAssigned()
    {
        return this.dateAssigned;
    }

    public void setDateAssigned(Date dateAssigned)
    {
        this.dateAssigned = new Date(dateAssigned.getTime())
        {
            @Serial
            private static final long serialVersionUID = 2753455820327491277L;

            @Override
            public String toString()
            {
                return new SimpleDateFormat("dd MMMM yyyy").format(this);
            }
        };
    }

    // Override toString() for displaying in a JavaFX TableView
    @Override
    public String toString()
    {
        return this.name;
    }
}
