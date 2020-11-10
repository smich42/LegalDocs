package legal;

public class LCourt
{
    private String name;
    private String address;

    public LCourt(String name)
    {
        this(name, new String());
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
