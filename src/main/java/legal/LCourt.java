package legal;

import java.io.Serial;

/*
 * Responsibilities:
 * - Represents a legal court.
 */

public class LCourt implements java.io.Serializable
{
    @Serial
    private static final long serialVersionUID = 18530754483472595L;

    private String name;
    private CourtTypes type;

    // Overload constructor to assign UNSPECIFIED as the default court type
    public LCourt(String name)
    {
        this(name, CourtTypes.UNSPECIFIED);
    }

    public LCourt(String name, CourtTypes type)
    {
        this.setName(name);
        this.setType(type);
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

    public CourtTypes getType()
    {
        return (this.type != null) ? (this.type) : (CourtTypes.UNSPECIFIED);
    }

    public void setType(CourtTypes type)
    {
        this.type = type;
    }

    // Override toString() for displaying in a JavaFX TableView
    @Override
    public String toString()
    {
        return this.name;
    }

    public enum CourtTypes
    {
        UNSPECIFIED, CRIMINAL, CIVIL, ADMINISTRATIVE;

        // Override default enum toString() to beautify output
        // toString() is used by javafx.scene.control.ChoiceBox to print enum values
        @Override
        public String toString()
        {
            return switch (this)
                    {
                        case CRIMINAL -> "Criminal Court";
                        case CIVIL -> "Civil Court";
                        case ADMINISTRATIVE -> "Administrative Court";
                        case UNSPECIFIED -> "Unspecified";
                    };
        }
    }
}
