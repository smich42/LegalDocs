package legal;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class DocumentManager
{
    private List<Document> docs;
    private DocumentLevTrie docsLT;

    public DocumentManager()
    {
        this.docs = new ArrayList<>();
        this.docsLT = new DocumentLevTrie();
    }

    public void addDocument(Document doc)
    {
        this.docs.add(doc);
        this.docsLT.insert(doc);
    }

    public List<Document> getDocuments()
    {
        // Return immutable version of document list
        return Collections.unmodifiableList(this.docs);
    }

    public List<Document> searchDocuments(String searchQuery)
    {
        // Return immutable version of the list of matching documents
        return Collections.unmodifiableList(this.docsLT.getMatchingDocs(searchQuery, 2));
    }
}
