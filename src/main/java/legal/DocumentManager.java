package legal;

import java.util.*;

public class DocumentManager
{
    private final List<Document> docs;

    public DocumentManager()
    {
        this.docs = new ArrayList<>();
    }

    public void addDocument(Document doc)
    {
        this.docs.add(doc);
    }

    public List<Document> getDocuments()
    {
        // Return immutable version of document list
        return Collections.unmodifiableList(this.docs);
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
                results.add(docMatcher.getDocument());
            }
        }

        // Return immutable version of the list of matching documents
        return results;
    }
}
