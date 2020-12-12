import java.io.File;
import java.sql.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.List;
import document.*;
import legal.*;

public final class Test
{
    private static final Random rand = new Random();

    private Test()
    {
    }

    public static void testSearch(DocumentManager dm)
    {
        Scanner in = new Scanner(System.in);

        System.out.print("> ");
        String S = in.nextLine();

        while (!S.isEmpty() && !S.isBlank())
        {
            String[] queries = S.split(" OR ");

            long start = System.nanoTime();

            Map<String, List<Document>> matches = dm.search(queries, 2);

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
    }

    public static void addWikiDocuments(DocumentManager dm)
    {
        LCourt courtA = new LCourt("CourtA");
        LCourt courtB = new LCourt("CourtB");
        LCourt courtC = new LCourt("CourtC");
        LCourt courtD = new LCourt("CourtD");

        LClient clientA = new LClient("ClientA", "clientA@client.com", "0123456789");
        LClient clientB = new LClient("ClientB", "clientB@client.com", "0123456789");
        LClient clientC = new LClient("ClientC", "clientC@client.com", "0123456789");
        LClient clientD = new LClient("ClientD", "clientD@client.com", "0123456789");

        LCase caseA = new LCase("CaseA", courtA, clientA, new Date(1L));
        LCase caseB = new LCase("CaseB", courtB, clientB, new Date(2L));
        LCase caseC = new LCase("CaseC", courtC, clientC, new Date(3L));
        LCase caseD = new LCase("CaseD", courtD, clientD, new Date(4L));

        LCase[] lCases = {caseA, caseB, caseC, caseD};

        File directory = new File("C:/Users/stavr/Downloads/files/");

        for (File f : Objects.requireNonNull(directory.listFiles()))
        {
            dm.addDocument(new Document(f, lCases[rand.nextInt(4)]));
        }
    }
}
