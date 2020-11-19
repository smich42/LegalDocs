package legal;

import java.io.IOException;
import java.util.Collection;
import javafx.util.Pair;

import java.util.Map;
import java.util.HashMap;

import java.util.LinkedList;

public class DocumentMatcher
{
    private class Node
    {
        // Slightly unconventional node structure;
        // I am saving characters in the nodes--the edges have no values
        private char val;
        private String term;
        private Map<Character, Node> children;

        public Node(char val)
        {
            this.val = val;
            this.term = null;
            this.children = new HashMap<>();
        }

        public char getVal()
        {
            return this.val;
        }

        public boolean isTerm()
        {
            return this.term != null;
        }

        public void addTerm(String term)
        {
            this.term = term;
        }

        public String getTerm()
        {
            return this.term;
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
    Document doc;

    public DocumentMatcher(Document doc)
    {
        this.doc = doc;
        
        root = new Node('\0');

        try
        {
            for (String term : doc.listTerms())
            {
                this.insert(term);
            }
        }
        catch (IOException e)
        {
        }
    }

    private int findMinDistance(String pattern)
    {
        LinkedList<Pair<String, Integer>> patternDistances = computeSubtree(this.root, pattern, new int[0]);
        int min = Integer.MAX_VALUE;

        for (Pair<String, Integer> p : patternDistances)
        {
            if (p.getValue() < min)
            {
                min = p.getValue();
            }
        }

        return min;
    }

    public boolean isMatch(String pattern, int maxDistance)
    {
        // Wiktor Stribizew's answer at stackoverflow.com/a/56060289/7970195
        String[] subPatterns = pattern.split("[\\p{P}\\p{S}]");
        
        double avgSubDistance = 0;

        for (String subPattern : subPatterns)
        {
            avgSubDistance += (double)findMinDistance(subPattern);
        }

        avgSubDistance /= subPatterns.length;

        int patternDistance = findMinDistance(Document.removePunctuation(pattern));
        
        // True if sufficiently close to whole pattern or to average of individual distances
        return Math.min((double) patternDistance, avgSubDistance) <= maxDistance;
    }

    private LinkedList<Pair<String, Integer>> computeSubtree(Node curNode, String pattern, int[] prevRow)
    {
        int[] curRow = new int[pattern.length() + 1];
        
        // When we are at the root node we have 0 characters for the name string
        // Therefore we need i insertions for the ith position of the row
        if (curNode == this.root)
        {
            for (int i = 0; i < curRow.length; ++i)
            {
                curRow[i] = i;
            }
        }
        else
        {
            curRow[0] = prevRow[0] + 1;
            
            // Make matching non-case-sensitive
            char currentPos = Character.toLowerCase(curNode.getVal());

            for (int i = 1; i <= pattern.length(); ++i)
            {
                char patternPos = Character.toLowerCase(pattern.charAt(i - 1));

                // Substitution cost is 1 if the two characters are different,
                // 0 otherwise
                int subCost = (currentPos != patternPos) ? 1 : 0;

                curRow[i] = Math.min(
                    Math.min(
                        prevRow[i] + 1, // Deletion
                        curRow[i - 1] + 1 // Insertion 
                    ),
                    prevRow[i - 1] + subCost // Substitution
                );
            }
        }

        LinkedList<Pair<String, Integer>> results = new LinkedList<>();

        if (curNode.isTerm())
        {
            Pair<String, Integer> result = new Pair<>(
                curNode.getTerm(),
                curRow[curRow.length - 1] // Levenshtein distance
            );

            results.add(result);
        }

        // Recursively join lists to construct a list of all the results in this branch
        for (Node nextNode : curNode.getChildren())
        {
            results.addAll(computeSubtree(nextNode, pattern, curRow));
        }

        return results;
    }

    public void insert(String term)
    {
        term = Document.removePunctuation(term);

        // Start from the root node
        Node cur = root;

        for (char c : term.toCharArray())
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

        cur.addTerm(term);
    }

    public Document getDocument()
    {
        return this.doc;
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