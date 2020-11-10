package legal;

public class LClient
{
    private String name;
    private String email;
    private String phone;

    public LClient(String name)
    {
        this(name, new String(), new String());
    }

    public LClient(String name, String email, String phone)
    {
        this.setName(name);
        this.setEmail(email);
        this.setPhone(phone);
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return this.email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhone()
    {
        return this.phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }
}
