package document;

/*
 * IMPORTS
 * package legal: For classes representing cases, clients and courts
 * packages java.io, java.nio: For I/O operations
 * package java.util: Used for collections, date handling and other utilities provided by Java
 * package org.nustaq.serialization: Provides a faster implementation of object serialisation/deserialisation
 */


import legal.LCase;
import legal.LClient;
import legal.LCourt;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/*
 * RESPONSIBILITIES
 * - Handles operations on the Document objects that are managed by the solution.
 */

public class DocumentManager
{
    // Path for serialised documents
    public static final String SERIALISATION_PATH = System.getProperty("user.home") + "\\Downloads\\serial\\docs\\";
    // Name for serialised documents
    public static final String SERIALISATION_NAME = "documents";

    // Names and files of documents currently managed in a set to quickly check for duplicates
    private final Set<String> docNames;
    private final Set<File> docFiles;

    // Used for the same purpose as docNames and docFiles, i.e. duplicate checking
    // It is not a set because it is convenient for cases/clients/courts to map objects with their names
    // They can therefore be quickly indexed only using their name
    private final Map<String, LCase> lCases;
    private final Map<String, LClient> lClients;
    private final Map<String, LCourt> lCourts;

    // List of all documents managed
    private List<Document> docs;

    public DocumentManager()
    {
        this.docs = new ArrayList<>();

        this.docNames = new HashSet<>();
        this.docFiles = new HashSet<>();

        this.lCases = new LinkedHashMap<>();
        this.lClients = new LinkedHashMap<>();
        this.lCourts = new LinkedHashMap<>();

        // Deserialise documents and config if they exist
        this.addFromSerialised();
    }

    /* Creates a directory for serialisation purposes */
    private static boolean makeSerialisationPath(String path)
    {
        File pathFile = new File(path);

        if (!pathFile.exists())
        {
            // Try to create directory
            if (pathFile.mkdirs())
            {
                System.out.println("Created directory for serialisation at '" + SERIALISATION_PATH + "'");
            }
            else
            {
                System.out.println("Failed to create directory for serialisation at '" + SERIALISATION_PATH + "'");
                return false; // Unsuccessful creation
            }
        }

        return true; // Successful creation
    }

    /* Creates directories for serialisation, if they do not exist*/
    public static boolean makeSerialisationDirectories()
    {
        return makeSerialisationPath(DocumentManager.SERIALISATION_PATH)
                && makeSerialisationPath(DocumentMatcher.SERIALISATION_PATH);
    }

    /* Returns the name of the serialisation file for the Document list */
    private static String getSerialFilename()
    {
        return SERIALISATION_PATH + SERIALISATION_NAME + ".serl_DOCS";
    }

    /* Searches for exact matches of a query in all documents */
    public List<Document> searchExactly(String searchQuery)
    {
        return this.search(searchQuery, 0);
    }

    /* Searches for approximate matches of a query in all documents (fuzzy search) with default tolerance (maxDistance) */
    public List<Document> search(String searchQuery)
    {
        return this.search(searchQuery, 2);
    }

    /* Searches for matches of a query in all documents, overloaded to include tolerance (maxDistance) */
    public List<Document> search(String searchQuery, int maxDistance)
    {
        // No need to search if searchQuery is blank; just return all documents, as it is sure to match them
        if (searchQuery == null || searchQuery.isBlank())
        {
            // Return by value for immutability
            return List.copyOf(this.docs);
        }

        Map<String, List<Document>> results = this.search(new String[]{searchQuery}, maxDistance);

        // Empty list if no results
        if (!results.containsKey(searchQuery))
        {
            return new ArrayList<>();
        }

        return results.get(searchQuery);
    }

    /*
     * Searches for matches of an array of queries in all documents
     * This is the most generic search implementation that is called by all other specific search methods
     */
    public Map<String, List<Document>> search(String[] searchQueries, int maxDistance)
    {
        // Map queries to matching documents
        Map<String, List<Document>> results = new HashMap<>();
        DocumentMatcher docMatcher;

        // Iterate over every document for search
        for (Document doc : this.docs)
        {
            docMatcher = new DocumentMatcher(doc);

            for (String searchQuery : searchQueries)
            {
                // Check every query for match
                if (docMatcher.matches(searchQuery, maxDistance))
                {
                    // If results already have documents matching the query, simply add to the already existing list
                    if (results.containsKey(searchQuery))
                    {
                        results.get(searchQuery).add(doc);
                    }
                    else
                    {
                        List<Document> queryResults = new LinkedList<>();
                        queryResults.add(doc);

                        results.put(searchQuery, queryResults);
                    }
                }
            }
        }

        // Return immutable version of the list of matching documents
        return results;
    }

    /*
     * Sorts the document list of this object by the category given by user.
     * Category should be one of the following.
     *      - Document.class (sorting by document name)
     *      - LCase.class (sorting by case name)
     *      - LClient.class (sorting by court name)
     *      - Date.class (sorting by date of assignment)
     */
    public void sortByCategory(Class<?> category)
    {
        // Do not sort if there are no documents
        if (!this.hasDocuments())
        {
            return;
        }

        // Create a new list to be sorted, as original list should not be modified until sorting is done
        List<Document> sorted = new ArrayList<>(this.docs);

        if (category != Document.class
                && category != LCase.class
                && category != LClient.class
                && category != LCourt.class
                && category != Date.class)
        {
            throw new IllegalArgumentException("'" + category.toString() + "': no such LCategory");
        }

        this.quicksort(sorted, 0, sorted.size() - 1, category);

        // Set this.docs to the sorted list
        this.docs = sorted;
    }

    /* Implementation of quicksort for O(log n) sorting */
    private void quicksort(List<Document> toSort, int l, int r, Class<?> category)
    {
        if (l < r)
        {
            // Divide-and-conquer design
            int split = this.partition(toSort, l, r, category);

            // Recursively sort sub-arrays
            this.quicksort(toSort, l, split - 1, category);
            this.quicksort(toSort, split + 1, r, category);
        }
    }

    /* Partitions arrays into two sub-arrays such that the elements in one are of lower order than all in the other */
    private int partition(List<Document> toSort, int l, int r, Class<?> category)
    {
        // Logic behind code below outlined & explained in pseudocode in the Design stage
        Document curDoc = toSort.get(r);
        int index = l - 1;

        for (int i = l; i < r; ++i)
        {
            Document indexDoc = toSort.get(i);

            LCase curCase = curDoc.getCase();
            LCase indexCase = indexDoc.getCase();

            // If sorting by date
            if (category == Date.class)
            {
                // Start with default date
                Date curDate = new Date(0);
                Date indexDate = new Date(0);

                if (curCase != null)
                {
                    // Change date to that of curCase
                    curDate = curCase.getDateAssigned();
                }

                if (indexCase != null)
                {
                    // Change date to that of indexCase
                    indexDate = indexCase.getDateAssigned();
                }

                // Swap documents if needed
                if (indexDate.compareTo(curDate) > 0
                        || (indexDate.compareTo(curDate) == 0
                        && indexDoc.getName().compareTo(curDoc.getName()) <= 0))
                {
                    index += 1; // Move on to next document
                    Collections.swap(toSort, index, i);
                }
            }
            else
            {
                // Start with default values
                String curName = "";
                String indexName = "";

                if (category == Document.class)
                {
                    // Change names to that of the documents
                    curName = curDoc.getName();
                    indexName = indexDoc.getName();
                }
                else if (category == LCase.class)
                {
                    // Change name to that of the cases, if possible
                    if (curCase != null)
                    {
                        curName = curCase.getName();
                    }
                    if (indexCase != null)
                    {
                        indexName = indexCase.getName();
                    }
                }
                else if (category == LClient.class)
                {
                    // Change name to that of the client, if possible
                    if (curCase != null && curCase.getClient() != null)
                    {
                        curName = curCase.getClient().getName();
                    }
                    if (indexCase != null && indexCase.getClient() != null)
                    {
                        indexName = indexCase.getClient().getName();
                    }
                }
                else if (category == LCourt.class)
                {
                    // Change name to that of the court, if possible
                    if (curCase != null && curCase.getCourt() != null)
                    {
                        curName = curCase.getCourt().getName();
                    }
                    if (indexCase != null && indexCase.getCourt() != null)
                    {
                        indexName = indexCase.getCourt().getName();
                    }
                }

                // Swap documents if needed
                if (indexName.compareTo(curName) < 0 || (indexName.compareTo(curName) == 0 && indexDoc.getName().compareTo(
                        curDoc.getName()) <= 0))
                {
                    index += 1; // Move on to next document
                    Collections.swap(toSort, index, i);
                }
            }
        }

        Collections.swap(toSort, index + 1, r);

        return index + 1; // Return partition index
    }

    /*
     * Closing procedure for the DocumentManager
     * Current responsibilities only include document serialisation, but can be expanded to run any required procedure
     */
    public void close()
    {
        this.serialiseDocuments();
    }

    /* Deletes document and file associated with it */
    public void deleteDocumentAndFile(Document doc)
    {
        // Try do delete file
        if (Document.deleteAssociatedFileOf(doc))
        {
            // Remove document associations if file successfully deleted
            this.removeDocument(doc);
        }
        else
        {
            System.out.println("Failed to delete document '" + doc.getName() + "'");
        }
    }

    /* Removes document and cleans up associations */
    public void removeDocument(Document doc)
    {
        this.docs.remove(doc);
        this.docNames.remove(doc.getName());
        this.docFiles.remove(doc.getFile());
    }

    /* Adds document to this DocumentManager */
    public boolean addDocument(Document doc)
    {
        if (this.docs == null)
        {
            this.docs = new ArrayList<>();
        }

        // Check if document can be added (duplicate checks, etc.)
        if (this.canAddDoc(doc))
        {
            // Add document
            this.docs.add(doc);

            // Add name, file for duplicate checking
            this.docFiles.add(doc.getFile());
            this.docNames.add(doc.getName());

            // Add case details
            this.addCase(doc.getCase());

            return true; // Successful execution
        }
        else
        {
            System.out.println("Could not add document for file with name " + doc.getFileName() + ".");
            return false; // Unsuccessful execution
        }
    }

    /* Adds document and also serialises it */
    public void addAndSerialiseDocument(Document doc)
    {
        if (this.addDocument(doc))
        {
            // Serialise only if document successfully added
            DocumentMatcher.serialiseTrieOf(doc);
        }
    }

    /* Checks if document can be added */
    public boolean canAddDoc(Document doc)
    {
        // Do not add if file does not exist
        if (doc == null || !doc.getFile().exists())
        {
            return false;
        }

        // Do not add if file or name is duplicate
        return !this.docFiles.contains(doc.getFile())
                && !this.docNames.contains(doc.getName());
    }

    /* Returns a list of all documents currently in the manager */
    public List<Document> listDocuments()
    {
        // Return by value for immutability
        return List.copyOf(this.docs);
    }

    /* Checks if this DocumentManager contains documents */
    public boolean hasDocuments()
    {
        return (this.docs != null) && (!this.docs.isEmpty());
    }

    /* Adds case to this DocumentManager */
    public void addCase(LCase lCase)
    {
        // Do not add null
        if (lCase == null)
        {
            return;
        }

        // Check for duplicate case
        if (!this.lCases.containsKey(lCase.getName()))
        {
            // Add case
            this.lCases.put(lCase.getName(), lCase);

            this.addClient(lCase.getClient());
            this.addCourt(lCase.getCourt());
        }
    }

    /* Returns a list of all cases currently in the manager */
    public List<LCase> listCases()
    {
        // Return by value for immutability
        return List.copyOf(this.lCases.values());
    }

    /* Adds client to this DocumentManager */
    public void addClient(LClient lClient)
    {
        // Do not add null
        if (lClient == null)
        {
            return;
        }

        // Check for duplicate client
        if (!this.lClients.containsKey(lClient.getName()))
        {
            this.lClients.put(lClient.getName(), lClient);
        }
    }

    /* Returns a list of all clients currently in the manager */
    public List<LClient> listClients()
    {
        // Return by value for immutability
        return List.copyOf(this.lClients.values());
    }

    /* Adds court to this DocumentManager */
    public void addCourt(LCourt lCourt)
    {
        // Do not add null
        if (lCourt == null)
        {
            return;
        }

        // Check for duplicate court
        if (!this.lCourts.containsKey(lCourt.getName()))
        {
            this.lCourts.put(lCourt.getName(), lCourt);
        }
    }

    /* Returns a list of all courts currently in the manager */
    public List<LCourt> listCourts()
    {
        // Return by value for immutability
        return List.copyOf(this.lCourts.values());
    }

    /* Restores serialised configuration, if it exists */
    public List<Document> deserialiseDocuments()
    {
        String serialName = getSerialFilename();
        File serialFile = new File(serialName);

        // Try to deserialise only if serialised configuration file exists
        if (serialFile.exists())
        {
            try (InputStream fSer = new FileInputStream(serialName); FSTObjectInput inSer = new FSTObjectInput(fSer))
            {
                // Read serialised document list
                List<Document> deserialised = (List<Document>) inSer.readObject();

                System.out.println("Recovered serialised documents");

                return deserialised;
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    /* Serialises current documents configuration */
    private void serialiseDocuments()
    {
        // Ensure path exists
        if (makeSerialisationDirectories())
        {
            String serialName = getSerialFilename();

            try (OutputStream fSer = new FileOutputStream(serialName, false);
                 FSTObjectOutput outSer = new FSTObjectOutput(fSer))
            {
                // Serialise document list to disc
                outSer.writeObject(this.docs);

                System.out.println("Serialised documents");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /* Deletes serialised configuration */
    public void deleteSerialisedDocuments()
    {
        String serialName = getSerialFilename();
        File serialFile = new File(serialName);

        // Delete file
        if (serialFile.delete())
        {
            System.out.println("Deleted serialised documents");
        }
        else
        {
            System.out.println("Failed to delete serialised documents");
        }
    }

    /* Populates document list with deserialised documents */
    private void addFromSerialised()
    {
        // Recover serialised documents
        List<Document> deserialisedDocs = this.deserialiseDocuments();

        if (deserialisedDocs != null)
        {
            // Add all documents
            for (Document doc : deserialisedDocs)
            {
                // Re-serialise to update any out-of-date document tries
                this.addAndSerialiseDocument(doc);
            }
        }
    }

    /* Imports the serialised configuration from a file */
    public void importSerialised(File importFile)
    {
        // Ensure path exists
        if (makeSerialisationDirectories())
        {
            String serialName = getSerialFilename();

            try
            {
                // Copy configuration file to serialisation path
                Files.copy(importFile.toPath(), Paths.get(serialName), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /* Exports configuration file to user desktop */
    public boolean exportSerialised()
    {
        String serialName = getSerialFilename();
        File serialFile = new File(serialName);

        if (serialFile.exists())
        {
            try
            {
                // Get user desktop path
                Path desktopPath = Paths.get(System.getProperty("user.home") + "/Desktop/EXPORT_CONFIG.serl_DOCS");

                // Copy and overwrite
                Files.copy(serialFile.toPath(), desktopPath, StandardCopyOption.REPLACE_EXISTING);

                return true; // Successful export
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return false; // Unsuccessful export
    }
}
