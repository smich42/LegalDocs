package legal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

public class DocumentManager
{
    static final String SERIALISATION_PATH = "C:/Users/stavr/Downloads/serial/docs/";
    static final String SERIALISATION_NAME = "documents";

    private List<Document> docs;

    public DocumentManager()
    {
        List<Document> deserialisedDocs = this.deserialiseDocuments();

        if (deserialisedDocs != null)
        {
            this.docs = deserialisedDocs;
        }
        else
        {
            this.docs = new ArrayList<>();
        }
    }

    public void close()
    {
        this.serialiseDocuments();
    }

    public void addDocument(Document doc)
    {
        this.docs.add(doc);
    }

    public List<Document> listDocuments()
    {
        // Return immutable version of document list
        return Collections.unmodifiableList(this.docs);
    } 

    public String getSerialFilename()
    {
        return SERIALISATION_PATH + SERIALISATION_NAME + ".serl_DOCS";
    }

    public List<Document> deserialiseDocuments()
    {
        String serialName = this.getSerialFilename();

        File serialFile = new File(serialName);

        if (serialFile.exists())
        {
            try (InputStream fSer = new FileInputStream(serialName);
                 FSTObjectInput inSer = new FSTObjectInput(fSer))
            {
                @SuppressWarnings("unchecked")
                List<Document> dser = (List<Document>) inSer.readObject();

                System.out.println("Recovered serialised documents");

                return dser;
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void serialiseDocuments()
    {
        File serialPath = new File(SERIALISATION_PATH);

        if (!serialPath.exists())
        {
            if (serialPath.mkdirs())
            {
                System.out.println("Created directory for serialisation at '" + SERIALISATION_PATH + "'");
            }
            else
            {
                System.out.println("Failed to create directory for serialisation at '" + SERIALISATION_PATH + "'");
                return; // Stop method execution if directory does not exist
            }
        }

        String serialName = this.getSerialFilename();

        try (OutputStream fSer = new FileOutputStream(serialName, false);
             FSTObjectOutput outSer = new FSTObjectOutput(fSer))
        {
            outSer.writeObject(this.docs);

            System.out.println("Serialised documents");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void deleteSerialisedTrie()
    {
        String serialName = this.getSerialFilename();

        File serialFile = new File(serialName);
        
        if (serialFile.delete())
        {
            System.out.println("Deleted serialised documents");
        }
        else
        {
            System.out.println("Failed to delete serialised documents");
        }
    }

    public List<Document> searchExactly(String searchQuery)
    {
        return this.search(searchQuery, 0);
    }

    public List<Document> search(String searchQuery)
    {
        return this.search(searchQuery, 2);
    }

    public List<Document> search(String searchQuery, int maxDistance)
    {
        List<Document> results = new LinkedList<>();
        DocumentMatcher docMatcher;

        for (Document doc : this.docs)
        {
            docMatcher = new DocumentMatcher(doc);

            if (docMatcher.matches(searchQuery, maxDistance))
            {
                results.add(doc);
            }
        }

        // Return immutable version of the list of matching documents
        return results;
    }

    public Map<String, List<Document>> search(String[] searchQueries, int maxDistance)
    {
        Map<String, List<Document>> results = new HashMap<>();
        DocumentMatcher docMatcher;

        for (Document doc : this.docs)
        {
            docMatcher = new DocumentMatcher(doc);

            for (String searchQuery : searchQueries)
            {
                if (docMatcher.matches(searchQuery, maxDistance))
                {
                    if (!results.containsKey(searchQuery))
                    {
                        List<Document> queryResults = new LinkedList<>();
                        queryResults.add(doc);

                        results.put(searchQuery, queryResults);
                    }
                    else
                    {
                        results.get(searchQuery).add(doc);
                    }
                }
            }
        }

        // Return immutable version of the list of matching documents
        return results;
    }
}
