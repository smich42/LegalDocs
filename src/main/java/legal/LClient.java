package legal;

/*
 * IMPORTS
 * org.apache.commons.validator.routines.EmailValidator: Used for validation
 * java.io.Serial: Used for serialisation (saving configuration)
 */

import org.apache.commons.validator.routines.EmailValidator;

import java.io.Serial;

/*
 * RESPONSIBILITIES
 * - Represents the client of a case
 * - Contains helper validator methods.
 */

public class LClient implements java.io.Serializable
{
    @Serial
    private static final long serialVersionUID = 1195714164291152059L;

    private String name;
    private String email;
    private String phone;

    public LClient(String name)
    {
        this.setName(name);
    }

    public LClient(String name, String email, String phone)
    {
        this.setName(name);
        this.setEmail(email);
        this.setPhone(phone);
    }

    public static boolean validateEmail(String email)
    {
        if (email == null || email.isBlank())
        {
            return true;
        }

        // Prefer Apache Commons validator over custom implementation
        EmailValidator validator = EmailValidator.getInstance();

        return validator.isValid(email);
    }

    public static boolean validatePhone(String phone)
    {
        if (phone == null || phone.isBlank())
        {
            return true; // No phone is a valid phone
        }

        char[] phoneChars = phone.toCharArray();

        if (phoneChars.length != 10)
        {
            return false;
        }

        // Number invalid if it contains non-digit characters
        for (char c : phoneChars)
        {
            if (!Character.isDigit(c))
            {
                return false;
            }
        }

        return true;
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

    public String getPhone()
    {
        return this.phone;
    }

    public void setPhone(String phone)
    {
        if (validatePhone(phone))
        {
            this.phone = phone;
        }
        else
        {
            throw new IllegalArgumentException("Invalid phone number '" + phone + "'");
        }
    }

    // Override toString() for displaying in a JavaFX TableView
    @Override
    public String toString()
    {
        return this.name;
    }
}
