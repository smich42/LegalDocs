package legal;

import java.io.File;

public class Document
{
    public enum DocumentTypes
    {
        PLAINTEXT, PDF, IMAGE
    }

    private File file;
    private String name;
    private LCase lCase;

    public Document(File file, LCase lCase)
    {
        this(file, new String(), lCase);
    }

    public Document(File file, String name, LCase lCase)
    {
        this.file = file;
        this.setName(name);
        this.setCase(lCase);
    }

    public String getFileName()
    {
        return this.file.getName();
    }

    public String getRelativePath()
    {
        return this.file.getPath();
    }

    public String getFullPath()
    {
        return this.file.getAbsolutePath();
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public LCase getCase()
    {
        return this.lCase;
    }

    public void setCase(LCase lCase)
    {
        this.lCase = lCase;
    }
}