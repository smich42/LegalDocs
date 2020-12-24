import java.io.File;
import java.sql.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;
import java.util.List;
import document.*;
import legal.*;

public final class Utilities
{
    private static final SplittableRandom rand = new SplittableRandom();

    private Utilities()
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
        LCourt courtA = new LCourt("Court A");
        LCourt courtB = new LCourt("Court B");
        LCourt courtC = new LCourt("Court C");
        LCourt courtD = new LCourt("Court D");

        LClient clientA = new LClient("Client A", "clientA@client.com", "0123456789");
        LClient clientB = new LClient("Client B", "clientB@client.com", "0123456789");
        LClient clientC = new LClient("Client C", "clientC@client.com", "0123456789");
        LClient clientD = new LClient("Client D", "clientD@client.com", "0123456789");

        LCase caseA = new LCase("Case A", courtA, clientA, new Date(TimeUnit.SECONDS.toMillis(rand.nextLong(1_600_000_000L))));
        LCase caseB = new LCase("Case B", courtB, clientB, new Date(TimeUnit.SECONDS.toMillis(rand.nextLong(1_600_000_000L))));
        LCase caseC = new LCase("Case C", courtC, clientC, new Date(TimeUnit.SECONDS.toMillis(rand.nextLong(1_600_000_000L))));
        LCase caseD = new LCase("Case D", courtD, clientD, new Date(TimeUnit.SECONDS.toMillis(rand.nextLong(1_600_000_000L))));

        LCase[] lCases = {caseA, caseB, caseC, caseD};

        File directory = new File("C:/Users/stavr/Downloads/files/");

        for (File f : Objects.requireNonNull(directory.listFiles()))
        {
            dm.addDocument(new Document(f, lCases[rand.nextInt(4)]));
        }
    }
}
