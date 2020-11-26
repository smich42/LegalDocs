import java.io.File;
import java.util.Scanner;

import legal.*;

public class Main
{
    public static void main(String[] args)
    {
        DocumentManager dm = new DocumentManager();

        File directory = new File("C:\\Users\\stavr\\Desktop\\files2");
        
        for (File f : directory.listFiles())
        {
            dm.addDocument(new Document(f));
        }

        Scanner in = new Scanner(System.in);
        String S;
        
        do
        {
            System.out.print("> ");
            
            S = in.nextLine();
            
            long start = System.nanoTime();

            for (Document match : dm.searchDocuments(S))
            {
                System.out.println(match.getFileName());
            }

            long duration = (System.nanoTime() - start);
            
            System.out.println("[" + duration / 1000000000 + " s elapsed]");
            System.out.println();

        } while (!S.isEmpty() && !S.isBlank());


        // DocumentManager dm = new DocumentManager();

        // File A = new File("C:/Users/stavr/Desktop/A.txt");
        // File B = new File("C:/Users/stavr/Desktop/B.txt");
        // File C = new File("C:/Users/stavr/Desktop/C.txt");

        // dm.addDocument(new Document(A));
        // dm.addDocument(new Document(B));
        // dm.addDocument(new Document(C));

        // for (Document match : dm.searchDocuments("Hello.world", 0))
        // {
        //     System.out.println(match.getFileName());
        // }

        // System.out.println("------");

        // for (Document match : dm.searchDocuments("Hello.world", 2))
        // {
        //     System.out.println(match.getFileName());
        // }

        in.close();
    }
}