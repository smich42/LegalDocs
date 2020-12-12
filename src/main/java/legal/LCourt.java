package legal;

public class LCourt implements java.io.Serializable
{
    private static final long serialVersionUID = 18530754483472595L;

    private String name;
    private String address;

    public LCourt(String name)
    {
        this.setName(name);
    }

    public LCourt(String name, String street, int number, String city)
    {
        this.setName(name);
        this.setAddress(street, number, city);
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

    public void setAddress(String street, int number, String city)
    {
        this.address = street + Integer.toString(number) + city;
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
