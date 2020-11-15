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

    public Document(File file)
    {
        this(file, null);
    }

    public Document(File file, LCase lCase)
    {
        this.file = file;
        
        // Set the name to the filename if no name is provided
        String fullname = file.getName();
        int extensionIndex = fullname.lastIndexOf('.');

        // Remove the extension
        String name = (extensionIndex != -1) ? fullname.substring(0, extensionIndex) : fullname;

        this.setName(name);
        this.setCase(lCase);
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