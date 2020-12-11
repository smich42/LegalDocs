package legal;

import org.apache.commons.validator.routines.EmailValidator;

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

    public void setEmail(String email)
    {
        if (validateEmail(email))
        {
            this.email = email;
        }
        else
        {
            throw new IllegalArgumentException("Invalid email address '" + email + "'");
        }
    }

    public boolean validateEmail(String email)
    {
        EmailValidator validator = EmailValidator.getInstance();

        return validator.isValid(email);
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
