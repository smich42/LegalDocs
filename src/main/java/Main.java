import legal.Document;
import legal.DocumentManager;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        DocumentManager dm = new DocumentManager();

        // File directory = new File("C:/Users/stavr/Downloads/files/");

        // for (File f : Objects.requireNonNull(directory.listFiles()))
        // {
        //     dm.addDocument(new Document(f));
        // }

        Scanner in = new Scanner(System.in);

        System.out.print("> ");
        String S = in.nextLine();

        while (!S.isEmpty() && !S.isBlank())
        {            
            String[] queries = S.split(" OR ");

            long start = System.nanoTime();

            Map<String, List<Document>> matches = dm.search(queries, 0);

            for (String query : queries)
            {
                if (!matches.containsKey(query))
                {
                    continue;
                }

                for (Document match : matches.get(query))
                {
                    System.out.println("Search string '" + query + "' found in '" + match.getFileName() + "' (" + match.getFullPath() + ")");
                }
            }

            long duration = (System.nanoTime() - start);
            System.out.println("[" + duration / 1_000_000_000 + " s elapsed]");

            System.out.print("\n> ");
            S = in.nextLine();
        }

        in.close();
        dm.close();
    }
}