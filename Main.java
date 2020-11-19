import java.io.File;
import java.util.List;

import legal.*;

public class Main
{
    public static void main(String[] args)
    {
        DocumentManager dm = new DocumentManager();

        File A = new File("C:/Users/stavr/OneDrive/Desktop/A.txt");
        File B = new File("C:/Users/stavr/OneDrive/Desktop/B.txt");
        File C = new File("C:/Users/stavr/OneDrive/Desktop/C.txt");

        dm.addDocument(new Document(A));
        dm.addDocument(new Document(B));
        dm.addDocument(new Document(C));

        for (Document match : dm.searchDocuments("Hello.world", 0))
        {
            System.out.println(match.getFileName());
        }

        System.out.println("------");

        for (Document match : dm.searchDocuments("Hello.world", 2))
        {
            System.out.println(match.getFileName());
        }
    }
}