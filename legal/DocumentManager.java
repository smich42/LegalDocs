package legal;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class DocumentManager
{
    private List<Document> docs;

    private static final String SERIALISED_PATH = "C:/Users/stavr/Desktop/serialised/";
    
    public DocumentManager()
    {
        this.docs = new ArrayList<>();
    }

    public void addDocument(Document doc)
    {
        this.docs.add(doc);

        long start = System.nanoTime();

        DocumentMatcher dm = new DocumentMatcher(doc);

        long duration1 = (System.nanoTime() - start);

        start = System.nanoTime();

        serialiseMatcher(dm);

        long duration2 = (System.nanoTime() - start);
       
        System.out.format("[Gen. %.1f s | Ser. %.2f s] ", (double)duration1 / 1000000000.0, (double)duration2 / 1000000000.0);
    }

    public List<Document> getDocuments()
    {
        // Return immutable version of document list
        return Collections.unmodifiableList(this.docs);
    }
    
    public static String generateSerialisedMatcherName(Document doc)
    {
        return SERIALISED_PATH + "MATCHER_" + DocumentMatcher.SEARCH_WORDS_MAX + "_" + doc.getLastModified() + "_" + doc.getName() + ".ser"; 
    }

    public static void serialiseMatcher(DocumentMatcher dm) 
    {
        Document doc = dm.getDocument();
        String filename = generateSerialisedMatcherName(doc); 

        File testExists = new File(filename);
        
        if (testExists.exists())
        {
            System.out.println("DocumentMatcher for '" + doc.getName() + "' already exists.");
            return;
        }

        try (FileOutputStream file = new FileOutputStream(filename); 
            ObjectOutputStream out = new ObjectOutputStream(file);)
        {
            out.writeObject(dm);
            System.out.println("DocumentMatcher for '" + doc.getName() + "' serialised.");
        } 
        catch(IOException e)
        {
            System.out.println(e.getLocalizedMessage()); 
        }
    }

    public static DocumentMatcher deserialiseMatcher(Document doc)
    {
        DocumentMatcher dm = null;

        try (FileInputStream file = new FileInputStream(generateSerialisedMatcherName(doc)); 
            ObjectInputStream in = new ObjectInputStream(file);)
        {
            dm = (DocumentMatcher)in.readObject();
            System.out.println("DocumentMatcher for '" + doc.getName() + "' deserialised.");
        }
        catch(IOException | ClassNotFoundException e) 
        {
            System.out.println(e.getLocalizedMessage()); 
        }

        return dm;
    }

    public List<Document> searchDocuments(String searchQuery)
    {
        return searchDocuments(searchQuery, 2);
    }

    public List<Document> searchDocuments(String searchQuery, int maxDistance)
    {
        List<Document> matchingDocs = new LinkedList<>();

        for (Document doc : this.docs)
        {
            DocumentMatcher dm = deserialiseMatcher(doc);

            if (dm == null)
            {
                dm = new DocumentMatcher(doc);
                serialiseMatcher(dm);
            }

            if (dm.isMatch(searchQuery, maxDistance))
            {
                matchingDocs.add(dm.getDocument());
            }
        }

        // Return immutable version of the list of matching documents
        return Collections.unmodifiableList(matchingDocs);
    }
}
