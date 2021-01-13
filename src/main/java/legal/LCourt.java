package legal;

public class LCourt implements java.io.Serializable
{
    private static final long serialVersionUID = 18530754483472595L;
    private String name;
    private CourtTypes type;
    public LCourt(String name)
    {
        this(name, CourtTypes.UNSPECIFIED);
    }

    public LCourt(String name, CourtTypes type)
    {
        this.setName(name);
        this.setType(type);
    }

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

    @Override
    public String toString()
    {
        return this.name;
    }

    public enum CourtTypes
    {
        UNSPECIFIED, CRIMINAL, CIVIL, ADMINISTRATIVE;

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
