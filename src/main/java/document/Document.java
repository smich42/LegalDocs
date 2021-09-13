package document;

/*
 * IMPORTS
 * package legal: For classes representing cases, clients and courts
 * packages java.io, java.nio: For I/O operations
 * java.util.Date: Represents Date objects  (e.g. assignment date)
 * java.util.List, LinkedList: Necessary to use linked lists
 * java.security: Used for hashing
 * java.math.BigInteger: Used to represent large numbers in hashing
 */

import legal.LCase;
import legal.LClient;
import legal.LCourt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/*
 * RESPONSIBILITIES
 * - Represents a document managed by the solution.
 * - Encapsulates managed File objects
 */

public class Document implements java.io.Serializable
{
    @Serial
    private static final long serialVersionUID = 3182448916075483593L;

    private final File file; // File associated with the document
    private String name;
    private LCase lCase;

    // Allow document creation without a file
    // Newly-created documents do not have an assigned LCase by default so they use this constructor
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

    /* Utility method that returns the filename of a file stripped of the file extension */
    public static String filenameNoExt(File file)
    {
        String fullname = file.getName();
        int extensionIndex = fullname.lastIndexOf('.');

        // Remove the extension
        return (extensionIndex != -1) ? fullname.substring(0, extensionIndex) : fullname;
    }

    /* Utility method that returns the extension of a file, including the period before it */
    public static String getFileExtension(File file)
    {
        String fullname = file.getName();
        int extensionIndex = fullname.lastIndexOf('.');

        // Return extension
        return (extensionIndex != -1) ? fullname.substring(extensionIndex) : "";
    }

    /* Utility method that replaces all punctuation in a string with some character */
    public static String replacePunctuation(String S, String replacement)
    {
        // Dimitar II's answer at https://stackoverflow.com/a/53996890/7970195
        S = S.replaceAll("\\R", " "); // Replace newlines with space

        S = S.replaceAll("\\s+", " "); // Replace double spaces with single space

        return S.replaceAll("[^-\\w\\s]", replacement).trim(); // Catch all punctuation except hyphen
    }

    /* Utility method that removes all punctuation from a string */
    public static String removePunctuation(String S)
    {
        return replacePunctuation(S, "");
    }

    /*
     * Deletes file associated with a Document,
     * returning true if the file was successfully deleted, false otherwise
     */
    public static boolean deleteAssociatedFileOf(Document document)
    {
        return document.file.delete();
    }

    /* Returns file associated with this Document */
    public File getFile()
    {
        return this.file;
    }

    /*
     * Returns the name of the file, hashed with SHA-256 for good collision resistance
     * Useful in document serialisation
     */
    public String getHashedName()
    {
        String toHash = this.getFullPath();

        try
        {
            // Using MrBitwise's answer at https://stackoverflow.com/a/62401723/7970195
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

    /* Returns the final full path for serialisation, stripped of the file extension */
    public String getSerialNameNoExt(String serialisationPath)
    {
        DocumentManager.makeSerialisationDirectories();

        File checkInvalidFile = new File(serialisationPath);

        // Check path to throw exception if invalid
        if (!checkInvalidFile.isDirectory())
        {
            throw new InvalidPathException(serialisationPath, "Path is not a valid directory name.");
        }

        // Hash name
        String hashedName = this.getHashedName();

        if (hashedName == null)
        {
            return null;
        }

        // Append at end of the serialisation path
        return serialisationPath + hashedName;
    }

    /* Returns final full path for non-attributes serialisation */
    public String getSerialFilename(String serialisationPath)
    {
        return this.getSerialNameNoExt(serialisationPath) + ".serl";
    }

    /* Returns final full path for attributes serialisation */
    public String getSerialAttributesFilename(String serialisationPath)
    {
        return this.getSerialNameNoExt(serialisationPath) + ".attr";
    }

    /* Returns all text in the file associated with this document*/
    private String getFileText() throws IOException
    {
        // Call specialised method if file is PDF.
        if (getFileExtension(this.file).equals(".pdf"))
        {
            return this.getPDFText(this.file);
        }

        // Read using java.util.Scanner if file is TXT.
        if (getFileExtension(this.file).equals(".txt"))
        {
            StringBuilder fileText = new StringBuilder();

            try (Scanner in = new Scanner(this.file, StandardCharsets.UTF_8))
            {
                while (in.hasNext())
                {
                    // Insert space between words
                    fileText.append(" ").append(in.next());
                }
            }

            return fileText.toString();
        }

        return "";
    }

    /* Extracts the text from a pdf file */
    private String getPDFText(File file)
    {
        String fileExtension = getFileExtension(file);

        // Check file extension to ensure it is a pdf file
        if (fileExtension.equals(".pdf"))
        {
            try
            {
                // Use Apache PDFBox for simple text extraction
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

    /*
     * Returns a LinkedList containing all terms of the file.
     * A "term" is comprised of one or more words.
     * The "offset" is the number of words in the first term.
     * e.g. if (input string) = "Fair is foul, and foul is fair: Hover through the fog and filthy air." (Macbeth, Act I, Scene I)
     *         (words per term) = 3
     *         (offset = 2),
     *      the list will be {"Fair is", "foul, and foul", "is fair: Hover", "through the fog", "and filthy air."}
     * Note that punctuation is not removed at this point.
     */
    public List<String> listTerms(int wordsPerTerm, int offset)
    {
        List<String> terms = new LinkedList<>();

        offset %= wordsPerTerm; // Offset obviously cannot be greater than the number of words per term

        // Convert file text to InputStream to read with Scanner
        // Use UTF-8 encoding for Greek, Arabic, etc.
        try (InputStream fileTextStream = new ByteArrayInputStream(this.getFileText().getBytes());
             Scanner in = new Scanner(fileTextStream, StandardCharsets.UTF_8))
        {
            // Read offset words
            for (int i = 0; i < offset; ++i)
            {
                if (!in.hasNext())
                {
                    return terms;
                }

                in.next();
            }

            // Loop breaks only when Scanner.hasNext is false
            // It may occur during the nested iteration
            while (true)
            {
                StringBuilder termToAdd = new StringBuilder();

                // Iterate to get input for one term
                for (int i = 0; i < wordsPerTerm; ++i)
                {
                    if (in.hasNext())
                    {
                        // Read next word
                        String S = in.next().trim();

                        if (S.isEmpty())
                        {
                            continue;
                        }

                        // Add word to term
                        termToAdd.append(S).append(" ");
                    }
                    else
                    {
                        return terms;
                    }
                }

                // Finally, add term to list
                terms.add(termToAdd.toString());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return terms;
    }

    /* Returns a string for the full (absolute) path of the file associated with this Document */
    public String getFullPath()
    {
        return this.file.getAbsolutePath();
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

    // Override toString() for displaying in a JavaFX TableView
    @Override
    public String toString()
    {
        return this.name;
    }
}
