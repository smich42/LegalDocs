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
    
    private List<LCase> lCases;
    private List<LClient> lClients;
    private List<LCourt> lCourts;

    public DocumentManager()
    {
        this.lCases = new ArrayList<>();
        this.lClients = new ArrayList<>();
        this.lCourts = new ArrayList<>();

        List<Document> deserialisedDocs = null; // this.deserialiseDocuments();

        if (deserialisedDocs != null)
        {
            this.docs = deserialisedDocs;
        }
        else
        {
            this.docs = new ArrayList<>();
        }
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

    public List<Document> sortByCategory(Class<?> category)
    {
        List<Document> sorted = new ArrayList<>(this.docs);

        if (category != LCase.class &&
            category != LClient.class &&
            category != LCourt.class &&
            category != Date.class)
        {
            throw new IllegalArgumentException("'" + category.toString() + "': no such LCategory");
        }

        quicksort(sorted, 0, sorted.size() - 1, category);

        return sorted;
    }

    private void quicksort(List<Document> toSort, int l, int r, Class<?> category)
    {
        if (l < r)
        {
            int split = partition(toSort, l, r, category);

            quicksort(toSort, l, split - 1, category);
            quicksort(toSort, split + 1, r, category);
        }
    }

    private int partition(List<Document> toSort, int l, int r, Class<?> category)
    {
        Document curDoc = toSort.get(r);
        int index = l - 1;

        for (int i = l; i < r; ++i)
        {
            Document indexDoc = toSort.get(i);

            LCase curCase = curDoc.getCase();
            LCase indexCase = indexDoc.getCase();

            if (category == Date.class)
            {
                Date curDate = curCase.getDateAssigned();
                Date indexDate = indexCase.getDateAssigned();

                if (indexDate.compareTo(curDate) < 0  ||
                    (indexDate.compareTo(curDate) == 0 && indexDoc.getName().compareTo(curDoc.getName()) <= 0))
                {
                    index += 1;
                    Collections.swap(toSort, index, i);
                }
            }
            else
            {
                String curName = "";
                String indexName = "";

                if (category == LCase.class)
                {
                    curName = curCase.getName();
                    indexName = indexCase.getName();
                }
                else if (category == LClient.class)
                {
                    curName = curCase.getClient().getName();
                    indexName = indexCase.getClient().getName();
                }
                else if (category == LCourt.class)
                {
                    curName = curCase.getCourt().getName();
                    indexName = indexCase.getCourt().getName();
                }

                if (indexName.compareTo(curName) < 0 ||
                    (indexName.compareTo(curName) == 0 && indexDoc.getName().compareTo(curDoc.getName()) <= 0))
                {
                    index += 1;
                    Collections.swap(toSort, index, i);
                }
            }
        }
        
        Collections.swap(toSort, index + 1, r);

        return index + 1;
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
        return Collections.unmodifiableList(this.docs);
    } 

    public void addCase(LCase lCase)
    {
        this.lCases.add(lCase);
    }

    public List<LCase> listCases()
    {
        return Collections.unmodifiableList(this.lCases);
    } 

    public void addClient(LClient lClient)
    {
        this.lClients.add(lClient);
    }

    public List<LClient> listClients()
    {
        return Collections.unmodifiableList(this.lClients);
    } 

    public void addCourt(LCourt lCourt)
    {
        this.lCourts.add(lCourt);
    }

    public List<LCourt> listCourts()
    {
        return Collections.unmodifiableList(this.lCourts);
    } 

    public String getSerialFilename()
    {
        return SERIALISATION_PATH + SERIALISATION_NAME + ".serl_DOCS";
    }
}
