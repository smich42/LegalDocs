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

        in.close();
    }
}