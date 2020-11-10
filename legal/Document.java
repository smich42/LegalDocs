package legal;

import java.io.File;

public class Document
{
    public enum DocumentTypes
    {
        PLAINTEXT, PDF, IMAGE
    }

    private File file;
    private String name;
    private LCase lCase;

    public static int levenshtein(String A, String B)
    {
        int[][] dp = new int[2][B.length() + 1];

        int prevRow = 0;
        int curRow = 1;

        for (int i = 0; i < B.length(); ++i)
        {
            dp[prevRow][i] = i;
        }
    
        for (int i = 0; i < A.length(); ++i)
        {
            dp[curRow][0] = i + 1;

            for (int j = 0; j < B.length(); ++j)
            {
                int sub = (A.charAt(i) != B.charAt(j)) ? 1 : 0;

                dp[curRow][j + 1] = Math.min(
                        Math.min(
                            dp[curRow][j] + 1,
                            dp[prevRow][j + 1] + 1
                        ),
                        dp[prevRow][j] + sub
                    );
            }
            
            prevRow = (prevRow == 1) ? 0 : 1;
            curRow = (curRow == 1) ? 0 : 1;
        }

        return dp[prevRow][B.length()];
    }

    public Document(File file, LCase lCase)
    {
        this(file, new String(), lCase);
    }

    public Document(File file, String name, LCase lCase)
    {
        this.file = file;
        this.setName(name);
        this.setCase(lCase);
    }

    public String getFileName()
    {
        return this.file.getName();
    }

    public String getRelativePath()
    {
        return this.file.getPath();
    }

    public String getFullPath()
    {
        return this.file.getAbsolutePath();
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public LCase getCase()
    {
        return this.lCase;
    }

    public void setCase(LCase lCase)
    {
        this.lCase = lCase;
    }
}