package document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import legal.LCase;
import legal.LClient;
import legal.LCourt;

public class Document implements java.io.Serializable
{
    private static final long serialVersionUID = 3182448916075483593L;

    private final File file;
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
        this.setName(filenameNoExt(file));
        this.setCase(lCase);
    }

    public Document(File file, String name, LCase lCase)
    {
        this.file = file;
        this.setName(name);
        this.setCase(lCase);
    }

    public static String filenameNoExt(File file)
    {
        String fullname = file.getName();
        int extensionIndex = fullname.lastIndexOf('.');

        // Remove the extension
        return (extensionIndex != -1) ? fullname.substring(0, extensionIndex) : fullname;
    }

    public static String getFileExtension(File file)
    {
        String fullname = file.getName();
        int extensionIndex = fullname.lastIndexOf('.');

        // Return extension
        return (extensionIndex != -1) ? fullname.substring(extensionIndex, fullname.length()) : "";
    }

    public static String replacePunctuation(String S, String replacement)
    {
        // Dimitar II's answer at https://stackoverflow.com/a/53996890/7970195
        S = S.replaceAll("\\R", " "); // Replace newlines with space

        S = S.replaceAll("\\s+", " "); // Replace double spaces with single space

        // Kobi's answer at https://stackoverflow.com/a/21209161/7970195
        return S.replaceAll("[^-\\w\\s]", replacement).trim(); // Catch all punctuation except hyphen
    }

    public static String removePunctuation(String S)
    {
        return replacePunctuation(S, "");
    }

    public boolean delete()
    {
        return this.file.delete();
    }

    public File getFile()
    {
        return this.file;
    }

    public String getHashedName()
    {
        String toHash = this.getFullPath();

        try
        {
            // From MrBitwise's answer at https://stackoverflow.com/a/62401723/7970195
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = md.digest(toHash.getBytes(StandardCharsets.UTF_8));
            BigInteger hashNumber = new BigInteger(1, hashBytes);

            return hashNumber.toString(16);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String getSerialNameNoExt(String serialisationPath)
    {
        String hashedName = this.getHashedName();

        if (hashedName == null)
        {
            return null;
        }

        return serialisationPath + hashedName;
    }

    public String getSerialFilename(String serialisationPath)
    {
        return this.getSerialNameNoExt(serialisationPath) + ".serl";
    }

    public String getSerialAttributesFilename(String serialisationPath)
    {
        return this.getSerialNameNoExt(serialisationPath) + ".attr";
    }

    private String getFileText() throws IOException
    {
        if (getFileExtension(this.file).equals(".pdf"))
        {
            return getPDFText(this.file);
        }

        if (getFileExtension(this.file).equals(".txt"))
        {
            StringBuilder fileText = new StringBuilder();

            try (Scanner in = new Scanner(this.file, StandardCharsets.UTF_8))
            {
                while (in.hasNext())
                {
                    fileText.append(" " + in.next());
                }
            }

            return fileText.toString();
        }

        return "";
    }

    private String getPDFText(File file)
    {
        String fileExtension = getFileExtension(file);

        if (fileExtension.equals(".pdf"))
        {
            try
            {
                PDDocument doc = PDDocument.load(file);
                PDFTextStripper pdfStripper = new PDFTextStripper();

                String text = pdfStripper.getText(doc);

                doc.close();

                return text;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return "";
    }

    public List<String> listTerms(int wordsPerTerm, int offset)
    {
        List<String> terms = new LinkedList<>();

        try (InputStream fileTextStream = new ByteArrayInputStream(this.getFileText().getBytes());
                Scanner in = new Scanner(fileTextStream, StandardCharsets.UTF_8))
        {
            for (int i = 0; i < offset; ++i)
            {
                if (!in.hasNext())
                {
                    return terms;
                }

                in.next();
            }

            while (true)
            {
                StringBuilder toAdd = new StringBuilder();

                for (int i = 0; i < wordsPerTerm; ++i)
                {
                    if (in.hasNext())
                    {
                        String S = in.next().trim();

                        if (S.isEmpty())
                        {
                            continue;
                        }

                        toAdd.append(S + " ");
                    }
                    else
                    {
                        return terms;
                    }

                }

                terms.add(toAdd.toString());
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

    public String getFileExtension()
    {
        String fullname = this.file.getName();
        int extensionIndex = fullname.lastIndexOf('.');

        if (extensionIndex == -1)
        {
            return null;
        }

        return fullname.substring(extensionIndex, fullname.length());
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getFileName()
    {
        return this.file.getName();
    }

    public long getDateModified()
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

    public LClient getClient()
    {
        if (this.getCase() == null)
        {
            return null;
        }

        return this.getCase().getClient();
    }

    public LCourt getCourt()
    {
        if (this.getCase() == null)
        {
            return null;
        }

        return this.getCase().getCourt();
    }

    public Date getDateAssigned()
    {
        if (this.getCase() == null)
        {
            return null;
        }

        return this.getCase().getDateAssigned();
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
