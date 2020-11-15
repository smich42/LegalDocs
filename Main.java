import java.io.File;
import java.util.Scanner;

import legal.*;

public class Main
{
    public static void main(String[] args)
    {
        DocumentManager dm = new DocumentManager();

        File directory = new File("C:/Users/stavr/OneDrive/Desktop/test");
        File[] files = directory.listFiles();

        for (File f : files)
        {
            dm.addDocument(new Document(f));
        }

        Scanner in = new Scanner(System.in);

        String search = in.nextLine();

        while (!search.isBlank())
        {
            System.out.println("------------");

            for (Document d : dm.searchDocuments(search))
            {
                System.out.println(d.getName());
            }

            System.out.println("------------");

            search = in.nextLine();
        }

        in.close();
    }
}