import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

import legal.*;

public class Main
{
    public static void main(String[] args)
    {
        DocumentManager dm = new DocumentManager();

        File directory = new File("C:/Users/stavr/Desktop/files2");

        for (File f : Objects.requireNonNull(directory.listFiles()))
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

            List<Document> matches = dm.searchExactly(S);

            for (Document match : matches)
            {
                System.out.println(match.getFileName());
            }

            long duration = (System.nanoTime() - start);
            System.out.println("[" + duration / 1_000_000_000 + " s elapsed]");
            System.out.println();
        }
        while (!S.isEmpty() && !S.isBlank());

        in.close();
    }
}