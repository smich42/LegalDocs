package legal;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Document implements java.io.Serializable
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
        if (file.isDirectory())
        {
            throw new IllegalArgumentException("Document file cannot be a directory.");
        }

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

    public static String replacePunctuation(String S, String replacement)
    {
        // Dimitar II's answer at https://stackoverflow.com/a/53996890/7970195
        S = S.replaceAll("\\R", " "); // Replace newlines with space

        S = S.replaceAll("\\s+", " "); // Replace double spaces with single space

        // Kobi's answer at https://stackoverflow.com/a/21209161/7970195
        return S.replaceAll("[^-\\w\\s]", replacement); // Catch all punctuation except hyphen
    }

    public static String removePunctuation(String S)
    {
        return replacePunctuation(S, "");
    }

    public List<String> listTerms(int wordsPerTerm, int offset)
    {
        List<String> terms = new LinkedList<>();

        try (Scanner input = new Scanner(this.file))
        {
            input.useDelimiter(" ");

            offset %= wordsPerTerm; // Offset cannot exceed words per term
            
            for (int i = 0; i < offset; ++i)
            {
                if (!input.hasNext())
                {
                    return Collections.unmodifiableList(terms);
                }

                terms.add(removePunctuation(input.next()));
            }
            
            while (true)
            {
                String toAdd = "";

                for (int i = 0; i < wordsPerTerm; ++i)
                {
                    if (!input.hasNext())
                    {
                        if (!toAdd.isEmpty())
                        {
                            terms.add(toAdd);
                        }

                        return Collections.unmodifiableList(terms);
                    }

                    if (!toAdd.isEmpty())
                    {
                        toAdd += " ";
                    }

                    toAdd += removePunctuation(input.next());
                }

                terms.add(toAdd);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return terms;
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

    public long getLastModified()
    {
        return this.file.lastModified();
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