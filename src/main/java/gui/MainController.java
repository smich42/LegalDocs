package gui;

/*
 * IMPORTS
 * package document: Used for document handling
 * package javafx.application: For basic operations on the JavaFX app
 * javafx.beans.binding.BooleanBinding: For binding properties of GUI elements to a binary condition
 * package javafx.collections: For collections necessary to build a JavaFX GUI
 * package javafx.fxml: For making use of FXML to create views
 * package javafx.scene: For basic operations on JavaFX graphics
 * package javafx.stage: For operations on the containers containing JavaFX controls, etc.
 * package legal: For classes representing cases, clients and courts
 * java.net.URL: For default FXML binding used by JavaFX initialisers
 * packages java.io, java.nio: For I/O operations
 */

import document.Document;
import document.DocumentManager;
import document.DocumentMatcher;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import legal.LCase;
import legal.LClient;
import legal.LCourt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/*
 * RESPONSIBILITIES
 * - Controls main view.
 * - Initialises JavaFX controls.
 */

public class MainController implements Initializable
{
    private final DocumentManager dm;

    private ObservableList<Document> docsList;
    private FilteredList<Document> docsFiltered;

    /* FXML bindings */
    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private TableView<Document> docTableView;

    @FXML
    private TableColumn<Document, String> nameCol;

    @FXML
    private TableColumn<Document, String> caseCol;

    @FXML
    private TableColumn<Document, String> clientCol;

    @FXML
    private TableColumn<Document, String> courtCol;

    @FXML
    private TableColumn<Document, String> dateCol;

    @FXML
    private Button addDocButton;

    @FXML
    private Button addDirButton;

    @FXML
    private Button detailsButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button importButton;

    @FXML
    private Button exportButton;

    @FXML
    private ChoiceBox<String> sortChoiceBox;

    @FXML
    private TextField filterTextField;

    @FXML
    private ChoiceBox<String> filterChoiceBox;

    @FXML
    private TextField searchTextField;

    @FXML
    private Button searchButton;

    public MainController(DocumentManager dm)
    {
        this.dm = dm;

        this.docsList = FXCollections.observableArrayList(dm.listDocuments());
        this.docsFiltered = new FilteredList<>(this.docsList, x -> true);
    }

    /* Returns stage for main view */
    private Stage getStage()
    {
        return (Stage) this.mainAnchorPane.getScene().getWindow();
    }

    /* Returns documents currently displayed in TableView */
    private List<Document> getDisplayedDocs()
    {
        // Return by value for immutability
        return new ArrayList<>(this.docsFiltered);
    }

    /* Initialises the TableView of documents */
    private void initTableView()
    {
        // Based on James_D's answer at https://stackoverflow.com/a/26565887/7970195
        this.docTableView.setRowFactory(e ->
        {
            TableRow<Document> row = new TableRow<>();

            // Catch double-click event
            row.setOnMouseClicked(event ->
            {
                // Require two clicks on a non-empty row of the table
                if (event.getClickCount() == 2 && !row.isEmpty())
                {
                    HostServices hostServices = (HostServices) this.getStage().getProperties().get("hostServices");
                    hostServices.showDocument(row.getItem().getFullPath()); // Open file with default programme
                }
            });

            return row;
        });

        // Bind values in table columns to Document attributes
        this.nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.caseCol.setCellValueFactory(new PropertyValueFactory<>("case"));
        this.clientCol.setCellValueFactory(new PropertyValueFactory<>("client"));
        this.courtCol.setCellValueFactory(new PropertyValueFactory<>("court"));
        this.dateCol.setCellValueFactory(new PropertyValueFactory<>("dateAssigned"));

        // Set docsFiltered as the source for the table data
        this.docTableView.setItems(this.docsFiltered);
    }

    /* Initialises filtering controls */
    private void initFilter()
    {
        // User can filter by Name, Case, Client or Court
        this.filterChoiceBox.getItems().addAll("Name", "Case", "Client", "Court");
        this.filterChoiceBox.setValue("Name");

        this.filterTextField.setOnKeyReleased(e ->
        {
            if (this.filterTextField.getText() == null || this.filterTextField.getText().isBlank())
            {
                this.docsFiltered.setPredicate(x -> true);
            }
            else
            {
                switch (this.filterChoiceBox.getValue())
                {
                    // Modify filter to match documents for which category name contains the filter text
                    // Not case sensitive
                    case "Name":
                        this.docsFiltered.setPredicate(x -> x.getName().toLowerCase().contains(this.filterTextField.getText().toLowerCase()));
                        break;

                    case "Case":
                        this.docsFiltered.setPredicate(x -> x.getCase() != null
                                && x.getCase().getName().toLowerCase().contains(this.filterTextField.getText().toLowerCase()));
                        break;

                    case "Client":
                        this.docsFiltered.setPredicate(x -> x.getClient() != null
                                && x.getClient().getName().toLowerCase().contains(this.filterTextField.getText().toLowerCase()));
                        break;

                    case "Court":
                        this.docsFiltered.setPredicate(x -> x.getCourt() != null
                                && x.getCourt().getName().toLowerCase().contains(this.filterTextField.getText().toLowerCase()));
                        break;

                    default:
                        break;
                }
            }
        });

        // Catch filter category change
        this.filterChoiceBox.getSelectionModel().selectedItemProperty().addListener((val, prev, next) ->
        {
            // Reset filter text
            this.filterTextField.setText("");
            // Reset filter to match all documents
            this.docsFiltered.setPredicate(x -> true);
        });
    }

    /* Initialises searching controls */
    private void initSearch()
    {
        this.searchTextField.setPromptText(this.searchTextField.getPromptText() + " (" + DocumentMatcher.SEARCH_WORDS_MAX + " words or fewer)");

        this.searchButton.setOnAction(e ->
        {
            Parent root = this.mainAnchorPane.getScene().getRoot();
            root.setCursor(Cursor.WAIT);

            // Adapted from Roland's answer at https://stackoverflow.com/a/28206116/7970195
            // Run search in new thread
            Thread thread = new Thread(() ->
            {
                try
                {
                    // Trim whitespace at end and start of text
                    String S = this.searchTextField.getText().trim();

                    // Replace punctuation with spaces and convert to char array
                    char[] charsOfS = Document.replacePunctuation(S, " ").toCharArray();

                    int wordCount = 1;

                    for (int i = 0; i < charsOfS.length - 1; ++i)
                    {
                        // Prevent increasing word count multiple times for input such as "word1   word2"
                        if (Character.isWhitespace(charsOfS[i]) && !Character.isWhitespace(charsOfS[i + 1]))
                        {
                            ++wordCount;
                        }
                    }

                    // Run search only if the word count does not exceed the maximum allowed
                    if (wordCount <= DocumentMatcher.SEARCH_WORDS_MAX)
                    {
                        List<Document> matches = this.dm.search(S);

                        for (Document doc : matches)
                        {
                            System.out.println(doc);
                        }

                        this.docsFiltered.setPredicate(matches::contains);
                    }
                    else
                    {
                        // Display alert dialogue if the word count exceeds the maximum allowed
                        Platform.runLater(() ->
                        {
                            Alert alert = new Alert(AlertType.ERROR);

                            alert.setTitle("Search error");
                            alert.setHeaderText("Please enter fewer than " + (DocumentMatcher.SEARCH_WORDS_MAX + 1) + " words.");

                            ButtonType OKButton = new ButtonType("OK", ButtonData.OK_DONE);

                            alert.getButtonTypes().setAll(OKButton);

                            alert.showAndWait();
                        });
                    }
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
                finally
                {
                    // Empty search text
                    Platform.runLater(() -> this.searchTextField.setText(""));
                    // Reset cursor
                    Platform.runLater(() -> root.setCursor(Cursor.DEFAULT));
                }
            });

            thread.start();
        });
    }

    /* Initialises sorting controls */
    private void initSort()
    {
        this.sortChoiceBox.getItems().addAll("Name sorting",
                "Case sorting",
                "Client sorting",
                "Court sorting",
                "Date sorting");
        this.sortChoiceBox.setValue("Previous sorting");

        this.sortChoiceBox.setOnAction(e ->
        {
            switch (this.sortChoiceBox.getValue())
            {
                case "Name sorting":
                    this.dm.sortByCategory(Document.class);
                    break;

                case "Case sorting":
                    this.dm.sortByCategory(LCase.class);
                    break;

                case "Client sorting":
                    this.dm.sortByCategory(LClient.class);
                    break;

                case "Court sorting":
                    this.dm.sortByCategory(LCourt.class);
                    break;

                case "Date sorting":
                    this.dm.sortByCategory(Date.class);
                    break;

                default:
                    break;
            }

            this.refreshFilteredDocs();
        });
    }

    /* Initialises document deletion controls */
    private void initDelete()
    {
        this.deleteButton.setOnAction(e ->
        {
            Document selected = this.docTableView.getSelectionModel().getSelectedItem();

            if (selected != null)
            {
                // Set up deletion dialogue
                Alert alert = new Alert(AlertType.NONE);

                alert.setTitle("Document deletion");
                alert.setHeaderText("Choose a way to delete '" + selected.getName() + ".'");

                // User can choose to either remove the document from the DocumentManager
                // or to delete both it and the associated file
                ButtonType deleteDocumentKeepFileButton = new ButtonType("Remove document (keeps file)");
                ButtonType deleteFileButton = new ButtonType("Delete file");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(deleteDocumentKeepFileButton, deleteFileButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.orElse(null) == deleteFileButton)
                {
                    this.dm.deleteDocumentAndFile(selected);
                }
                else if (result.orElse(null) == deleteDocumentKeepFileButton)
                {
                    this.dm.removeDocument(selected);
                }

                this.refreshFilteredDocs();
            }
        });
    }

    /* Initialises details controls */
    private void initDetails()
    {
        this.detailsButton.setOnAction(e ->
        {
            Document selected = this.docTableView.getSelectionModel().getSelectedItem();

            if (selected != null)
            {
                this.displayDetailsDialogue(selected);

                this.refreshFilteredDocs();
            }
        });
    }

    /* Initialises import/export buttons */
    private void initImportExport()
    {
        this.importButton.setOnAction(e ->
        {
            // Prompt user to choose the file to be imported
            FileChooser importFileChooser = new FileChooser();
            importFileChooser.setTitle("Select configuration file");

            importFileChooser.getExtensionFilters().add(new ExtensionFilter("Legal document config", "*.serl_DOCS"));

            // Display dialogue and assign selected file
            File selected = importFileChooser.showOpenDialog(this.getStage());

            if (selected != null)
            {
                this.dm.importSerialised(selected);
                this.addDocsToManager(this.dm.deserialiseDocuments());
            }

            this.refreshDocsDetails();
        });

        this.exportButton.setOnAction(e ->
        {
            if (this.dm.exportSerialised())
            {
                // Set up dialogue notifying user of successful exporting
                Alert alert = new Alert(AlertType.NONE);

                alert.setTitle("Configuration exported");
                alert.setHeaderText("Configuration exported to desktop.");

                ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);

                alert.getButtonTypes().setAll(okButton);

                // Display dialogue and wait for user to click "OK"
                alert.showAndWait();
            }
            else
            {
                // Set up dialogue notifying user of exporting error
                Alert alert = new Alert(AlertType.ERROR);

                alert.setTitle("Configuration not exported");
                alert.setHeaderText("An error occurred while exporting configuration. Please contact me at [email].");

                ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);

                alert.getButtonTypes().setAll(okButton);
                alert.showAndWait();
            }
        });
    }

    /* Handles adding documents to manager and displays a progress bar for search indexing */
    private void addDocsToManager(List<Document> docs)
    {
        // Load indexing view
        FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("indexingView.fxml"));

        try
        {
            Parent root = loader.load();

            Scene indexingScene = new Scene(root);
            Stage indexingStage = new Stage();

            indexingStage.setScene(indexingScene);
            indexingStage.setTitle("Indexing progress");

            indexingStage.show();

            ProgressBar indexingProgressBar = (ProgressBar) indexingScene.lookup("#indexingProgressBar");

            Thread thread = new Thread(() ->
            {
                double progress = 0.0;

                // How much progress the bar should record for every serialised ("indexed") document
                double step = 1.0 / (double) docs.size();

                root.setCursor(Cursor.WAIT);

                // Serialise documents added
                for (Document doc : docs)
                {
                    this.dm.addAndSerialiseDocument(doc);

                    indexingProgressBar.setProgress(progress);
                    // Increment current progress
                    progress += step;
                }

                Platform.runLater(() -> root.setCursor(Cursor.DEFAULT));
                Platform.runLater(indexingStage::close);
                Platform.runLater(this::refreshDocsDetails);
            });

            thread.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /* Initialises document creation button */
    private void initAddDoc()
    {
        this.addDocButton.setOnAction(e ->
        {
            // Prompt user to select a file
            FileChooser docFileChooser = new FileChooser();
            docFileChooser.setTitle("Select file");

            File selected = docFileChooser.showOpenDialog(this.getStage());

            if (selected != null)
            {
                Document docToAdd = new Document(selected);

                this.displayDetailsDialogue(docToAdd);
            }

            this.refreshDocsDetails();
        });
    }

    /* Initialises auto-add button */
    private void initAddDir()
    {
        this.addDirButton.setOnAction(e ->
        {
            // Prompt user to select directory
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select folder");

            File selected = dirChooser.showDialog(this.getStage());
            Path dirToAdd = selected.toPath();

            List<Document> docsToAdd = new LinkedList<>();

            try
            {
                // Create documents for all files in directories and sub-directories
                Files.walk(dirToAdd).filter(Files::isRegularFile).forEach(f -> docsToAdd.add(new Document(f.toFile())));

                this.addDocsToManager(docsToAdd);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        });
    }

    /* Displays dialogue */
    private void displayDetailsDialogue(Document selected)
    {
        DetailsController detailsController = new DetailsController(this, selected, this.dm);

        // Load details view and set controller
        FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("detailsView.fxml"));
        loader.setController(detailsController);

        try
        {
            Parent root = loader.load();

            Scene dialogScene = new Scene(root);
            Stage dialogStage = new Stage();

            dialogStage.setScene(dialogScene);
            dialogStage.setTitle("Document details");

            // Display details view and wait for user response
            dialogStage.showAndWait();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /* Refreshes the displayed filtered document table */
    public void refreshFilteredDocs()
    {
        List<Document> displayedDocs = this.getDisplayedDocs();

        this.docsList = FXCollections.observableArrayList(this.dm.listDocuments());
        this.docsFiltered = new FilteredList<>(this.docsList, displayedDocs::contains);

        this.docTableView.setItems(this.docsFiltered);
    }

    /* Updates the details of changed documents in the view */
    public void refreshDocsDetails()
    {
        this.docsList.clear();

        this.docsList = FXCollections.observableArrayList(this.dm.listDocuments());
        this.docsFiltered = new FilteredList<>(this.docsList, x -> true);

        this.docTableView.setItems(this.docsFiltered);
    }

    /* Initialises all components of the main view */
    @Override
    public void initialize(URL url, ResourceBundle resources)
    {
        this.initTableView();
        this.initFilter();
        this.initSearch();
        this.initSort();
        this.initDelete();
        this.initDetails();
        this.initImportExport();
        this.initAddDoc();
        this.initAddDir();

        BooleanBinding itemNotSelected = new BooleanBinding()
        {
            private final SelectionModel<Document> selectionModel = MainController.this.docTableView.getSelectionModel();

            {
                super.bind(this.selectionModel.selectedItemProperty());
            }

            @Override
            protected boolean computeValue()
            {
                return this.selectionModel.isEmpty();
            }
        };

        this.detailsButton.disableProperty().bind(itemNotSelected);
        this.deleteButton.disableProperty().bind(itemNotSelected);

        // Enable buttons if a document is selected
        if (!this.docTableView.getSelectionModel().isEmpty())
        {
            this.deleteButton.setDisable(false);
            this.detailsButton.setDisable(false);
        }
    }
}
