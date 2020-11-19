package legal;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

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
        String noExt = (extensionIndex != -1) ? fullname.substring(0, extensionIndex) : fullname;

        this.setName(noExt);
        this.setCase(lCase);
    }

    public Document(File file, String name, LCase lCase)
    {
        this.file = file;
        this.setName(name);
        this.setCase(lCase);
    }

    public static String removePunctuation(String S)
    {
        return S.replaceAll("\\p{P}", "");
    }

    public List<String> listTerms() throws IOException
    {
        List<String> terms = new LinkedList<>();

        try (Scanner input = new Scanner(this.file))
        {
            input.useDelimiter(" ");

            while (input.hasNext())
            {
                terms.add(removePunctuation(input.next()));
            }
        }

        return Collections.unmodifiableList(terms);
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

    public String getFileName()
    {
        return this.file.getName();
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