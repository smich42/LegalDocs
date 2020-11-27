import java.io.File;
import java.util.Scanner;

import javafx.util.Pair;
import legal.*;

public class Main
{
    public static void main(String[] args)
    {
        ChainList<Character> L1 = new ChainList<>();

        L1.add('A');
        L1.add('B');
        L1.add('C');

        L1.print();

        ChainList<Character> L2 = new ChainList<>();

        L2.add('D');

        L2.chainTo(L1);

        L2.add('E');
        L1.add('F');

        L1.print();
        L2.print();

        ChainList<Pair<String, Integer>> L3 = new ChainList<>();

        L3.add(new Pair<>("Pair 1", 1));
        L3.add(new Pair<>("Pair 2", 2));

        L3.print();

        /*DocumentManager dm = new DocumentManager();

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

        in.close();*/
    }
}