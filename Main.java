import legal.*;

public class Main
{
    public static void main(String[] args)
    {
        LevenshteinTrie lt = new LevenshteinTrie();

        lt.insert("AAA");
        lt.insert("ABB");
        lt.insert("BBB");
        lt.insert("BBC");
        lt.insert("BBCA");
        lt.insert("BBCC");
        lt.insert("ACB");
        lt.insert("ABBA");
        lt.insert("ABBC");
        
        lt.print();
    }
}