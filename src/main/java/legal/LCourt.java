package legal;

public class LCourt implements java.io.Serializable 
{
    public static final long serialVersionUID = 4L;

    private String name;
    private String address;

    public LCourt(String name)
    {
        this(name, "");
    }

    public LCourt(String name, String address)
    {
        this.setName(name);
        this.setAddress(address);
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAddress()
    {
        return this.address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }
}
