package legal;

import javafx.util.Pair;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentMatcher
{
    static final int SEARCH_WORDS_MAX = 3;
    static final String SERIALISATION_PATH = "C:/Users/stavr/Downloads/serial/";
    List<Node> nodes;
    Document doc;

    public DocumentMatcher(Document doc)
    {
        this.doc = doc;

        List<Node> deserialisedNodes = deserialise(this);

        if (deserialisedNodes == null)
        {
            this.buildTrie();
            serialise(this);
        }
        else
        {
            this.nodes = deserialisedNodes;
        }
    }

    public static List<Node> deserialise(DocumentMatcher dm)
    {
        String serialName = dm.doc.getSerialFilename(SERIALISATION_PATH);
        String attrsName = dm.doc.getSerialAttributesFilename(SERIALISATION_PATH);

        File serialFile = new File(serialName);
        File attrsFile = new File(attrsName);

        if (serialFile.exists() && attrsFile.exists())
        {
            try (InputStream fSer = new FileInputStream(serialName);
                 FSTObjectInput inSer = new FSTObjectInput(fSer);
                 FileInputStream fAttrs = new FileInputStream(attrsName);
                 DataInputStream inAttrs = new DataInputStream(fAttrs))
            {
                if (dm.doc.getDateModified() != inAttrs.readLong())
                {
                    System.out.println("Serialised trie for '" + dm.doc.getName() + "' outdated");

                    return null;
                }

                if (SEARCH_WORDS_MAX > inAttrs.readInt())
                {
                    System.out.println("Serialised trie for '" + dm.doc.getName() + "' too small");

                    return null;
                }

                @SuppressWarnings("unchecked")
                List<Node> dser = (List<Node>) inSer.readObject();

                System.out.println("Recovered serialised trie for '" + dm.doc.getName() + "'");

                return dser;
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void serialise(DocumentMatcher dm)
    {
        File serialPath = new File(SERIALISATION_PATH);

        if (!serialPath.exists())
        {
            if (serialPath.mkdir())
            {
                System.out.println("Created directory for serialisation at '" + SERIALISATION_PATH + "'");
            }
            else
            {
                System.out.println("Failed to create directory for serialisation at '" + SERIALISATION_PATH + "'");
                return; // Stop function if directory does not exist
            }
        }

        String serialName = dm.doc.getSerialFilename(SERIALISATION_PATH);
        String attrsName = dm.doc.getSerialAttributesFilename(SERIALISATION_PATH);

        if (serialName == null)
        {
            System.out.println("Could not generate filename to serialise " + dm.doc.getName());
            return;
        }

        try (OutputStream fSer = new FileOutputStream(serialName, false);
             FSTObjectOutput outSer = new FSTObjectOutput(fSer);
             OutputStream fAttrs = new FileOutputStream(attrsName, false);
             DataOutputStream outAttrs = new DataOutputStream(fAttrs))
        {
            outSer.writeObject(dm.nodes);
            outAttrs.writeLong(dm.doc.getDateModified());
            outAttrs.writeInt(SEARCH_WORDS_MAX);

            System.out.println("Serialised '" + dm.doc.getName() + "'");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void deleteSerialised(Document doc)
    {
        String serialName = doc.getSerialFilename(SERIALISATION_PATH);
        String attrsName = doc.getSerialAttributesFilename(SERIALISATION_PATH);

        File serialFile = new File(serialName);
        File attrsFile = new File(attrsName);

        if (serialFile.delete())
        {
            System.out.println("Deleted serialised trie for '" + doc.getName() + "'");
        }
        else
        {
            System.out.println("Failed to delete serialised trie for '" + doc.getName() + "'");
        }

        if (attrsFile.delete())
        {
            System.out.println("Deleted trie date file for '" + doc.getName() + "'");
        }
        else
        {
            System.out.println("Failed to delete trie date file for '" + doc.getName() + "'");
        }
    }

    public void buildTrie()
    {
        this.nodes = new ArrayList<>();

        Node root = new Node('\0');
        this.nodes.add(root);

        for (int wordsPerTerm = 1; wordsPerTerm <= SEARCH_WORDS_MAX; ++wordsPerTerm)
        {
            for (int offset = 0; offset < wordsPerTerm; ++offset)
            {
                for (String term : this.doc.listTerms(wordsPerTerm, offset))
                {
                    this.insert(term);
                }
            }
        }
    }

    public Node getNode(int index)
    {
        return this.nodes.get(index);
    }

    private int findMinDistance(String pattern)
    {
        ArrayList<Pair<String, Integer>> patternDistances = this.computeSubtree(this.getNode(0), pattern, new int[0]);

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

    public boolean contains(String term)
    {
        term = Document.removePunctuation(term.toLowerCase());

        if (term.isBlank() || term.isEmpty())
        {
            return false;
        }

        Node cur = this.getNode(0);

        for (char c : term.toCharArray())
        {
            if (!cur.hasChild(c))
            {
                return false;
            }

            cur = this.getNode(cur.getChild(c));
        }

        return true;
    }

    public boolean matches(String pattern, int maxDistance)
    {
        if (pattern.isBlank() || pattern.isEmpty())
        {
            return false;
        }

        if (maxDistance == 0)
        {
            return this.contains(pattern);
        }

        pattern = Document.replacePunctuation(pattern, " ");

        return this.findMinDistance(pattern) <= maxDistance;
    }

    private ArrayList<Pair<String, Integer>> computeSubtree(Node curNode, String pattern, int[] prevRow)
    {
        int[] curRow = new int[pattern.length() + 1];

        // When we are at the root node we have 0 characters for the name string
        // Therefore we need i insertions for the ith position of the row
        if (curNode == this.getNode(0))
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

        ArrayList<Pair<String, Integer>> results = new ArrayList<>();

        if (curNode.isTerm())
        {
            Pair<String, Integer> result = new Pair<>(
                    curNode.getTerm(),
                    curRow[curRow.length - 1] // Levenshtein distance
            );

            results.add(result);
        }

        // Recursively join lists to construct a list of all the results in this branch
        for (int nextIndex : curNode.getChildren())
        {
            Node nextNode = this.getNode(nextIndex);
            results.addAll(this.computeSubtree(nextNode, pattern, curRow));
        }

        return results;
    }

    public void insert(String term)
    {
        term = Document.removePunctuation(term.toLowerCase());

        // Start from the root node
        Node cur = this.getNode(0);

        for (char c : term.toCharArray())
        {
            // If a child node with value c already exists,
            // we should not add it again
            if (!cur.hasChild(c))
            {
                Node toAdd = new Node(c);

                this.nodes.add(toAdd);
                cur.addChild(toAdd, this.nodes.size() - 1);
            }

            // Move on deeper following the word path
            cur = this.getNode(cur.getChild(c));
        }

        cur.addTerm(term);
    }

    public Document getDocument()
    {
        return this.doc;
    }

    private static class Node implements java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        // Slightly unconventional node structure;
        // Characters saved in the nodes--edges have no values
        private final Map<Character, Integer> children;
        private final char val;
        private String term;

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

        public void addChild(Node child, int childIndex)
        {
            // Only add the child node only if there is no other child
            // with the same key
            if (!this.children.containsKey(child.getVal()))
            {
                this.children.put(child.getVal(), childIndex);
            }
        }

        public boolean hasChild(char val)
        {
            return this.children.containsKey(val);
        }

        public int getChild(char val)
        {
            if (this.hasChild(val))
            {
                return this.children.get(val);
            }

            return -1;
        }

        public Collection<Integer> getChildren()
        {
            return this.children.values();
        }
    }
}