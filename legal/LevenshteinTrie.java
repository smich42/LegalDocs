package legal;

import java.util.Collection;

import javafx.util.Pair;

import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.LinkedList;

public class LevenshteinTrie
{
    private class Node
    {
        // Slightly unconventional node structure;
        // I am saving characters in the nodes--the edges have no values
        private char val;
        private String name;
        private Map<Character, Node> children;

        public Node(char val)
        {
            this.val = val;
            this.name = null;
            this.children = new HashMap<>();
        }

        public char getVal()
        {
            return this.val;
        }

        public boolean hasName()
        {
            return this.name != null && !this.name.isEmpty();
        }

        public String getName()
        {
            return this.name;
        }

        public void addName(String name)
        {
            this.name = name;
        }

        public void addChild(Node child)
        {
            // Only add the child node only if there is no other child
            // with the same key
            if (!this.children.containsKey(child.getVal()))
            {
                this.children.put(child.getVal(), child);
            }
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

        public Collection<Node> getChildren()
        {
            return this.children.values();
        }
    }

    Node root;

    public LevenshteinTrie()
    {
        root = new Node('\0');
    }

    /* Vanilla Levenshtein implementation optimised to only use two matrix rows
    private static int levenshtein(String A, String B)
    {
        int[][] distanceMatrix = new int[2][B.length() + 1];

        int prevRow = 0;
        int curRow = 1;

        for (int i = 0; i < B.length(); ++i)
        {
            distanceMatrix[prevRow][i] = i;
        }
    
        for (int i = 0; i < A.length(); ++i)
        {
            distanceMatrix[curRow][0] = i + 1;

            for (int j = 0; j < B.length(); ++j)
            {
                int sub = (A.charAt(i) != B.charAt(j)) ? 1 : 0;

                distanceMatrix[curRow][j + 1] = Math.min(
                        Math.min(
                            distanceMatrix[curRow][j] + 1,
                            distanceMatrix[prevRow][j + 1] + 1
                        ),
                        distanceMatrix[prevRow][j] + sub
                    );
            }
            
            prevRow = (prevRow == 1) ? 0 : 1;
            curRow = (curRow == 1) ? 0 : 1;
        }

        return distanceMatrix[prevRow][B.length()];
    }
    */

    public List<String> getMatchingNames(String pattern, int maxDistance)
    {
        int[] firstRow = new int[pattern.length() + 1];


        LinkedList<Pair<String, Integer>> distances = computeNode(this.root, pattern, firstRow);
        LinkedList<String> results = new LinkedList<>();

        for (Pair<String, Integer> distance : distances)
        {
            if (distance.getValue() <= maxDistance)
            {
                results.add(distance.getKey());
            }
        }

        return results;
    }

    private LinkedList<Pair<String, Integer>> computeNode(Node curNode, String pattern, int[] prevRow)
    {
        int[] nextRow = new int[pattern.length() + 1];
        
        // When we are at the root node we have 0 characters for the name string
        // Therefore we need i insertions for the ith position of the row
        if (curNode == this.root)
        {
            for (int i = 0; i < nextRow.length; ++i)
            {
                nextRow[i] = i;
            }
        }
        else
        {
            nextRow[0] = prevRow[0] + 1;
            
            // Make matching non-case-sensitive
            char currentPos = Character.toLowerCase(curNode.getVal());

            for (int i = 1; i <= pattern.length(); ++i)
            {
                char patternPos = Character.toLowerCase(pattern.charAt(i - 1));

                // Substitution cost is 1 if the two characters are different,
                // 0 otherwise
                int subCost = (currentPos != patternPos) ? 1 : 0;

                nextRow[i] = Math.min(
                    Math.min(
                        prevRow[i] + 1, // Deletion
                        nextRow[i - 1] + 1 // Insertion 
                    ),
                    prevRow[i - 1] + subCost // Substitution
                );
            }
        }

        LinkedList<Pair<String, Integer>> results = new LinkedList<>();

        if (curNode.hasName())
        {
            Pair<String, Integer> result = new Pair<>(
                curNode.getName(),
                nextRow[nextRow.length - 1] // Levenshtein distance
            );

            results.add(result);
        }

        // Recursively join lists to construct a list of all the results in this branch
        for (Node nextNode : curNode.getChildren())
        {
            results.addAll(computeNode(nextNode, pattern, nextRow));
        }

        return results;
    }

    public void insert(String name)
    {
        // Start from the root node
        Node cur = root;

        for (char c : name.toCharArray())
        {
            // If a child node with value c already exists,
            // we should not add it again
            if (!cur.hasChild(c))
            {
                cur.addChild(new Node(c));
            }
            
            // Move on deeper following the word path
            cur = cur.getChild(c);
        }

        cur.addName(name);
    }

    public void print()
    {
        printRecurse(root, 0);
    }

    private void printRecurse(Node n, int level)
    {
        for (int i = 1; i < level - 1; ++i)
        {
            System.out.print("..");
        }

        if (level > 1)
        {
            System.out.print("|_");
        }

        // Do not print the empty root node
        if (level != 0)
        {
            System.out.println(n.getVal());
        }

        // Go down every branch recursively
        for (Node next : n.getChildren())
        {
            printRecurse(next, level + 1);
        }
    }
}