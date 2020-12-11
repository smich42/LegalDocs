import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import document.*;
import legal.*;

public class Main
{
    public static void main(String[] args)
    {
        mergeErrOutStreams();

        DocumentManager dm = new DocumentManager();

        try
        {
            // addTestDocuments(dm);

            for (Document doc : dm.sortByCategory(LCase.class))
            {
                System.out.println(doc.getName());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dm.close();
        }
    }

    public static void testSearch(DocumentManager dm)
    {
        File directory = new File("C:/Users/stavr/Downloads/files/");

        for (File f : Objects.requireNonNull(directory.listFiles()))
        {
            dm.addDocument(new Document(f));
        }

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
                    System.out.println(
                            "Search string '" + query + "' found in '" + match.getFileName() + "' (" + match.getFullPath() + ")");
                }
            }

            long duration = (System.nanoTime() - start);
            System.out.println("[" + duration / 1_000_000_000 + " s elapsed]");

            System.out.print("\n> ");
            S = in.nextLine();
        }

        in.close();
    }

    public static void addTestDocuments(DocumentManager dm)
    {
        LCourt courtD = new LCourt("CourtD");
        LCourt courtA = new LCourt("CourtA");
        LCourt courtB = new LCourt("CourtB");
        LCourt courtC = new LCourt("CourtC");

        dm.addCourt(courtA);
        dm.addCourt(courtB);
        dm.addCourt(courtC);
        dm.addCourt(courtD);

        LClient clientB = new LClient("ClientB", "client@client.com", "0123456789");
        LClient clientD = new LClient("ClientD", "client@client.com", "0123456789");
        LClient clientC = new LClient("ClientC", "client@client.com", "0123456789");
        LClient clientA = new LClient("ClientA", "client@client.com", "0123456789");

        dm.addClient(clientA);
        dm.addClient(clientB);
        dm.addClient(clientC);
        dm.addClient(clientD);

        LCase caseC = new LCase("CaseC", courtC, clientC, new Date(3L));
        LCase caseD = new LCase("CaseD", courtD, clientD, new Date(4L));
        LCase caseA = new LCase("CaseA", courtA, clientA, new Date(1L));
        LCase caseB = new LCase("CaseB", courtB, clientB, new Date(2L));

        dm.addCase(caseA);
        dm.addCase(caseB);
        dm.addCase(caseC);
        dm.addCase(caseD);

        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Amsterdam.txt"), "DocB2", caseB));
        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Ancient history.txt"), "DocB1", caseB));
        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Anti-Americanism.txt"), "DocB3", caseB));

        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Ashoka.txt"), "DocD1", caseD));
        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Atlantic slave trade.txt"), "DocD3", caseD));
        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Black Sabbath.txt"), "DocD2", caseD));

        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Azerbaijan.txt"), "DocA3", caseA));
        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Bahrain.txt"), "DocA1", caseA));
        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Battle of Belgium.txt"), "DocA2", caseA));

        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Berlin.txt"), "DocC1", caseC));
        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Bernie Sanders.txt"), "DocC3", caseC));
        dm.addDocument(new Document(new File("C:/Users/stavr/Downloads/files/Black Lives Matter.txt"), "DocC2", caseC));
    }

    public static void mergeErrOutStreams()
    {
        System.err.close();
        System.setErr(System.out);
    }
}
