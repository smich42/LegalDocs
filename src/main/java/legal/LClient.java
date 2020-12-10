package legal;

public class LClient implements java.io.Serializable
{
    private static final long serialVersionUID = 3L;

    private String name;
    private String email;
    private String phone;

    public LClient(String name)
    {
        this(name, "", "");
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

    // TODO: email validity check
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
