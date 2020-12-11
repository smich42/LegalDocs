package legal;

import java.util.Date;

public class LCase implements java.io.Serializable
{
    private static final long serialVersionUID = 2L;

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
        this.dateAssigned = dateAssigned;
    }
}
