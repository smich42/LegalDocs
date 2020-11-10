package legal;

import java.util.HashMap;

public class LevenshteinTrie
{
    private class Node
    {
        private char val;
        public HashMap<Character, Node> children;

        public Node(char val)
        {
            this.val = val;
            this.children = new HashMap<Character, Node>();
        }

        public char getVal()
        {
            return this.val;
        }

        public void addChild(Node child)
        {
            this.children.put(child.getVal(), child);
        }

        public boolean hasChild(char val)
        {
            return this.children.containsKey(val);
        }

        public Node getChild(char val)
        {
            if (this.hasChild(val))
            {
                return this.children.get(val);
            }
            return null;
        }
    }

    Node root;

    public LevenshteinTrie()
    {
        root = new Node('\0');
    }

    private static int levenshtein(String A, String B)
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

    public void insert(String S)
    {
        for (char c : S.toCharArray())
        {
            Node cur = root;
            
            if (!cur.hasChild(c))
            {
                cur.addChild(new Node(c));
            }
            
            cur = cur.getChild(c);
        }
    }
}