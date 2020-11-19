package legal;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

public class DocumentManager
{
    private List<Document> docs;
    private List<DocumentMatcher> docMatchers;

    public DocumentManager()
    {
        this.docs = new ArrayList<>();
        this.docMatchers = new LinkedList<>();
    }

    public void addDocument(Document doc)
    {
        this.docs.add(doc);
        this.docMatchers.add(new DocumentMatcher(doc));
    }

    public List<Document> getDocuments()
    {
        // Return immutable version of document list
        return Collections.unmodifiableList(this.docs);
    }
    
    public List<Document> searchDocuments(String searchQuery)
    {
        return searchDocuments(searchQuery, 2);
    }

    public List<Document> searchDocuments(String searchQuery, int maxDistance)
    {
        List<Document> matchingDocs = new LinkedList<>();

        for (DocumentMatcher docMatcher : this.docMatchers)
        {
            if (docMatcher.isMatch(searchQuery, maxDistance))
            {
                matchingDocs.add(docMatcher.getDocument());
            }
        }

        // Return immutable version of the list of matching documents
        return Collections.unmodifiableList(matchingDocs);
    }
}
