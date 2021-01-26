package document;

/*
 * IMPORTS
 * javafx.util.Pair: Pairs two items together; useful for pairing strings and their distance from the pattern
 * packages java.io: For I/O operations
 * package java.util: Used for collections, date handling and other utilities provided by Java
 * package org.nustaq.serialization: Provides a faster implementation of object serialisation/deserialisation
 */


import javafx.util.Pair;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.*;
import java.util.*;

/*
 * RESPONSIBILITIES
 * - Represents a trie searchable with Levenshtein distance
 */

public class DocumentMatcher
{
    // Maximum words per search string
    public static final int SEARCH_WORDS_MAX = 2;
    // Path for serialised tries
    public static final String SERIALISATION_PATH = System.getProperty("user.home") + "\\Downloads\\serial\\matchers\\";

    // Trie nodes
    List<Node> nodes;

    public DocumentMatcher()
    {
        // Prefer ArrayList implementation for List because of quick indexing
        this.nodes = new ArrayList<>();
    }

    public DocumentMatcher(Document doc)
    {
        // Rebuild if not up-to-date
        if (!upToDateSerialisedTrieExists(doc))
        {
            serialiseTrieOf(doc);
        }

        // Document is now guaranteed to have been serialised successfully
        this.nodes = deserialiseTrieOf(doc);
    }

    /* Restores pre-built DocumentMatcher object if serialised and up-to-date */
    public static List<Node> deserialiseTrieOf(Document doc)
    {
        // Full name for serialised file
        String serialName = doc.getSerialFilename(SERIALISATION_PATH) + "_DM";

        try (InputStream fSer = new FileInputStream(serialName);
             FSTObjectInput inSer = new FSTObjectInput(fSer))
        {
            // Only deserialise if trie is up-to-date
            if (upToDateSerialisedTrieExists(doc))
            {
                // Deserialise
                List<Node> dser = (List<Node>) inSer.readObject();

                System.out.println("Recovered serialised trie for '" + doc.getName() + "'");

                return dser;
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        // Null serialised trie could not be recovered
        return null;
    }

    /* Checks if a serialised trie for a document exists and is up-to-date */
    public static boolean upToDateSerialisedTrieExists(Document doc)
    {
        // Full filenames for serialised trie file and serialised attributes file
        String serialName = doc.getSerialFilename(SERIALISATION_PATH) + "_DM";
        String attrsName = doc.getSerialAttributesFilename(SERIALISATION_PATH) + "_DM";

        File serialFile = new File(serialName);
        File attrsFile = new File(attrsName);

        // First condition: serialised trie and attributes files exist
        if (serialFile.exists() && attrsFile.exists())
        {
            try (FileInputStream fAttrs = new FileInputStream(attrsName);
                 DataInputStream inAttrs = new DataInputStream(fAttrs))
            {
                // Second condition: date of file when serialised is the same as the current date of the file
                // Third condition: words per term for serialised trie is at least DocumentMatcher.SEARCH_WORDS_MAX
                return (doc.getDateModified() == inAttrs.readLong()) && (SEARCH_WORDS_MAX <= inAttrs.readInt());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return false;
    }

    /* Serialises the trie of a Document, if it does not already exist and is up-to-date */
    public static void serialiseTrieOf(Document doc)
    {
        if (upToDateSerialisedTrieExists(doc))
        {
            return;
        }

        File serialPath = new File(SERIALISATION_PATH);

        // Make serialisation directory if it does not already exist
        if (!serialPath.exists())
        {
            if (serialPath.mkdirs())
            {
                System.out.println("Created directory for serialisation at '" + SERIALISATION_PATH + "'");
            }
            else
            {
                System.out.println("Failed to create directory for serialisation at '" + SERIALISATION_PATH + "'");
                return; // Stop method execution if directory does not exist
            }
        }

        String serialName = doc.getSerialFilename(SERIALISATION_PATH) + "_DM";
        String attrsName = doc.getSerialAttributesFilename(SERIALISATION_PATH) + "_DM";

        try (OutputStream fSer = new FileOutputStream(serialName, false);
             FSTObjectOutput outSer = new FSTObjectOutput(fSer);
             OutputStream fAttrs = new FileOutputStream(attrsName, false);
             DataOutputStream outAttrs = new DataOutputStream(fAttrs))
        {
            // Create trie to be serialised
            DocumentMatcher toSerialise = new DocumentMatcher();
            toSerialise.buildTrie(doc);

            // Serialise trie
            outSer.writeObject(toSerialise.nodes);

            // Serialise attributes file with information allowing easy checking of whether the trie is up-to-date
            outAttrs.writeLong(doc.getDateModified());
            outAttrs.writeInt(SEARCH_WORDS_MAX);

            System.out.println("Serialised '" + doc.getName() + "'");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /* Deletes serialised trie for a Document, if it exists*/
    public static void deleteSerialisedTrie(Document doc)
    {
        String serialName = doc.getSerialFilename(SERIALISATION_PATH) + "_DM";
        String attrsName = doc.getSerialAttributesFilename(SERIALISATION_PATH) + "_DM";

        File serialFile = new File(serialName);
        File attrsFile = new File(attrsName);

        // Try to delete trie file
        if (serialFile.delete())
        {
            // Success
            System.out.println("Deleted serialised trie for '" + doc.getName() + "'");
        }
        else
        {
            System.out.println("Failed to delete serialised trie for '" + doc.getName() + "'");
        }

        // Try to delete attributes file
        if (attrsFile.delete())
        {
            // Success
            System.out.println("Deleted trie date file for '" + doc.getName() + "'");
        }
        else
        {
            System.out.println("Failed to delete trie date file for '" + doc.getName() + "'");
        }
    }

    /* Builds the trie using a Document */
    private void buildTrie(Document doc)
    {
        this.nodes = new ArrayList<>();

        Node root = new Node('\0'); // Root does not have a value
        this.nodes.add(root);

        // First two nested loops for every possible wordsPerTerm and offset combination
        for (int wordsPerTerm = 1; wordsPerTerm <= SEARCH_WORDS_MAX; ++wordsPerTerm)
        {
            for (int offset = 0; offset < wordsPerTerm; ++offset)
            {
                // For this combination of wordsPerTerm and offset, insert all terms in a document to the trie
                for (String term : doc.listTerms(wordsPerTerm, offset))
                {
                    this.insert(term);
                }
            }
        }
    }

    /* Returns the node at an index of the Node list */
    private Node getNode(int index)
    {
        return this.nodes.get(index);
    }

    /* Returns the Levenshtein (edit) distance of the closest fuzzy match to the "pattern" string */
    private int findMinDistance(String pattern)
    {
        // Compute the Levenshtein distances for a "subtree" starting from the root, i.e. the full trie.
        ArrayList<Pair<String, Integer>> patternDistances = this.computeSubtree(this.getNode(0), pattern, new int[0]);

        int min = Integer.MAX_VALUE;

        // Iterate over all pairs of terms and distances to find min distance
        for (Pair<String, Integer> p : patternDistances)
        {
            if (p.getValue() < min)
            {
                min = p.getValue();
            }
        }

        return min;
    }

    /* Returns true if term is in the trie, false otherwise */
    public boolean contains(String term)
    {
        // Terms in trie do not have punctuation
        // To lowercase for case insensitivity
        term = Document.removePunctuation(term.toLowerCase());

        // Immediately return true if pattern is empty
        if (term.isBlank() || term.isEmpty())
        {
            return true;
        }

        Node cur = this.getNode(0);

        // Try to follow path of characters of string term
        for (char c : term.toCharArray())
        {
            // If the path cannot be continued, term is not in the trie
            if (!cur.hasChild(c))
            {
                return false;
            }

            cur = this.getNode(cur.getChild(c));
        }

        // Successfully followed path
        return true;
    }

    /*
     * Returns true if the minimum Levenshtein distance of the pattern for this trie
     * is less than or equal to maxDistance
     */
    public boolean matches(String pattern, int maxDistance)
    {
        // Immediately return true if pattern is empty
        if (pattern.isBlank() || pattern.isEmpty())
        {
            return true;
        }

        // No need to run fuzzy search if maxDistance = 0
        if (maxDistance == 0)
        {
            return this.contains(pattern);
        }

        pattern = Document.replacePunctuation(pattern, " ");

        return this.findMinDistance(pattern) <= maxDistance;
    }

    /* Computes the Levenshtein distances for a "subtree" starting from curNode  */
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

            // To lowercase to make matching non-case-sensitive
            char currentPos = Character.toLowerCase(curNode.getVal());

            for (int i = 1; i <= pattern.length(); ++i)
            {
                char patternPos = Character.toLowerCase(pattern.charAt(i - 1));

                // Substitution cost is 1 if the two characters are different,
                // 0 otherwise
                int subCost = (currentPos != patternPos) ? 1 : 0;

                curRow[i] = Math.min(Math.min(prevRow[i] + 1, // Deletion
                        curRow[i - 1] + 1 // Insertion
                        ), prevRow[i - 1] + subCost // Substitution
                );
            }
        }

        ArrayList<Pair<String, Integer>> results = new ArrayList<>();

        // Only add to results if the current node contains a term, i.e. is a leaf node
        if (curNode.isTerm())
        {
            Pair<String, Integer> result = new Pair<>(curNode.getTerm(), curRow[curRow.length - 1]);

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

    /* Inserts a term to the trie */
    private void insert(String term)
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

        cur.setTerm(term);
    }

    /*
     * RESPONSIBILITIES
     *   - Represents trie nodes.
     */
    private static class Node implements java.io.Serializable
    {
        @Serial
        private static final long serialVersionUID = 4250386998186331854L;

        // Unconventional node structure for a trie;
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

        /* Accessor for value field */
        public char getVal()
        {
            return this.val;
        }

        /* Checks if this Node is a term (leaf) node */
        public boolean isTerm()
        {
            return this.term != null;
        }

        /* Returns the term of a node */
        public String getTerm()
        {
            return this.term;
        }

        /* Sets the term for a node */
        public void setTerm(String term)
        {
            this.term = term;
        }

        /* Connects this Node to a child node */
        public void addChild(Node child, int childIndex)
        {
            // Only add the child node only if there is no other child
            // with the same key
            if (!this.children.containsKey(child.getVal()))
            {
                this.children.put(child.getVal(), childIndex);
            }
        }

        /* Checks if this Node leads to another Node of some value */
        public boolean hasChild(char val)
        {
            return this.children.containsKey(val);
        }

        /* Returns the child Node of some value, if it exists */
        public int getChild(char val)
        {
            if (this.hasChild(val))
            {
                return this.children.get(val);
            }

            return -1;
        }

        /* Returns all the children Nodes */
        public Collection<Integer> getChildren()
        {
            return this.children.values();
        }
    }
}
