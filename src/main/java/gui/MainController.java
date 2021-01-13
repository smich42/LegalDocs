package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import document.Document;
import document.DocumentManager;
import document.DocumentMatcher;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import legal.LCase;
import legal.LClient;
import legal.LCourt;

public class MainController implements Initializable
{
    private final DocumentManager dm;
    private ObservableList<Document> docsList;
    private FilteredList<Document> docsFiltered;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private TableView<Document> docTableView;

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

    private Stage getStage()
    {
        return (Stage) this.mainAnchorPane.getScene().getWindow();
    }

    private List<Document> getDisplayedDocs()
    {
        return new ArrayList<>(this.docsFiltered);
    }

    private void initTableView()
    {
        // Based on James_D's answer at https://stackoverflow.com/a/26565887/7970195
        this.docTableView.setRowFactory(table ->
        {
            TableRow<Document> row = new TableRow<>();

            row.setOnMouseClicked(event ->
            {
                if (event.getClickCount() == 2 && !row.isEmpty())
                {

                    HostServices hostServices = (HostServices) this.getStage().getProperties().get("hostServices");

                    hostServices.showDocument(row.getItem().getFullPath()); // Open file with default programme
                }
            });

            return row;
        });

        this.docTableView.setOnMouseClicked(e ->
        {
            this.deleteButton.setDisable(false);
            this.detailsButton.setDisable(false);
        });

        TableColumn<Document, String> nameCol = (TableColumn<Document, String>) this.docTableView.getColumns().get(0);
        TableColumn<Document, String> caseCol = (TableColumn<Document, String>) this.docTableView.getColumns().get(1);
        TableColumn<Document, String> clientCol = (TableColumn<Document, String>) this.docTableView.getColumns().get(2);
        TableColumn<Document, String> courtCol = (TableColumn<Document, String>) this.docTableView.getColumns().get(3);
        TableColumn<Document, String> dateCol = (TableColumn<Document, String>) this.docTableView.getColumns().get(4);

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        caseCol.setCellValueFactory(new PropertyValueFactory<>("case"));
        clientCol.setCellValueFactory(new PropertyValueFactory<>("client"));
        courtCol.setCellValueFactory(new PropertyValueFactory<>("court"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateAssigned"));

        this.docTableView.setItems(this.docsFiltered);
    }

    private void initFilter()
    {
        this.filterChoiceBox.getItems().addAll("Name", "Case", "Client", "Court");
        this.filterChoiceBox.setValue("Name");

        this.filterTextField.setOnKeyReleased(e ->
        {
            switch (this.filterChoiceBox.getValue())
            {
                case "Name":
                    this.docsFiltered.setPredicate(x -> x.getName().toLowerCase().contains(this.filterTextField.getText().toLowerCase()));
                    break;

                case "Case":
                    this.docsFiltered.setPredicate(x -> x.getCase().getName().toLowerCase().contains(this.filterTextField.getText().toLowerCase()));
                    break;

                case "Client":
                    this.docsFiltered.setPredicate(x -> x.getClient().getName().toLowerCase().contains(this.filterTextField.getText().toLowerCase()));
                    break;

                case "Court":
                    this.docsFiltered.setPredicate(x -> x.getCourt().getName().toLowerCase().contains(this.filterTextField.getText().toLowerCase()));
                    break;

                default:
                    break;
            }
        });

        this.filterChoiceBox.getSelectionModel().selectedItemProperty().addListener((val, prev, next) ->
        {
            this.filterTextField.setText("");
            this.docsFiltered.setPredicate(x -> true);
        });
    }

    private void initSearch()
    {
        this.searchTextField.setPromptText(this.searchTextField.getPromptText() + " (" + DocumentMatcher.SEARCH_WORDS_MAX + " words or fewer)");

        this.searchButton.setOnAction(e ->
        {
            Parent root = this.mainAnchorPane.getScene().getRoot();

            root.setCursor(Cursor.WAIT);

            // Using Roland's answer at https://stackoverflow.com/a/28206116/7970195 for threading
            Thread thread = new Thread(() ->
            {
                try
                {
                    String S = this.searchTextField.getText().trim();
                    char[] charsOfS = Document.replacePunctuation(S, " ").toCharArray();

                    int wordCount = 1;

                    for (int i = 0; i < charsOfS.length - 1; ++i)
                    {
                        if (Character.isWhitespace(charsOfS[i]) && !Character.isWhitespace(charsOfS[i + 1]))
                        {
                            ++wordCount;
                        }
                    }

                    System.out.println(Document.replacePunctuation(S, " "));
                    System.out.println(wordCount);

                    if (wordCount <= DocumentMatcher.SEARCH_WORDS_MAX)
                    {
                        if (S.isEmpty() || S.isBlank())
                        {
                            this.docsFiltered.setPredicate(x1 -> true);
                        }
                        else
                        {
                            List<Document> matches = this.dm.search(S);

                            for (Document doc : matches)
                            {
                                System.out.println(doc);
                            }

                            this.docsFiltered.setPredicate(matches::contains);
                        }
                    }
                    else
                    {
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
                    Platform.runLater(() -> this.searchTextField.setText(""));
                    Platform.runLater(() -> root.setCursor(Cursor.DEFAULT));
                }
            });

            thread.start();
        });
    }

    private void initSort()
    {
        this.sortChoiceBox.getItems().addAll("Name sorting", "Case sorting", "Client sorting", "Court sorting", "Date sorting");
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

    private void initDelete()
    {
        this.deleteButton.setDisable(true);

        this.deleteButton.setOnAction(e ->
        {
            Document selected = this.docTableView.getSelectionModel().getSelectedItem();

            if (selected != null)
            {
                Alert alert = new Alert(AlertType.NONE);

                alert.setTitle("Document deletion");
                alert.setHeaderText("Choose a way to delete '" + selected.getName() + ".'");

                ButtonType deleteDocumentKeepFileButton = new ButtonType("Delete document and keep file");
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
                    this.dm.deleteDocument(selected);
                }

                this.refreshFilteredDocs();
            }
        });
    }

    private void initDetails()
    {
        this.detailsButton.setDisable(true);

        this.detailsButton.setOnAction(e ->
        {
            Document selected = this.docTableView.getSelectionModel().getSelectedItem();

            if (selected != null)
            {
                this.displayDetailsDialog(selected);

                this.refreshFilteredDocs();
            }
        });
    }

    private void initImportExport()
    {
        this.importButton.setOnAction(e ->
        {
            FileChooser importFileChooser = new FileChooser();
            importFileChooser.setTitle("Select import file");

            importFileChooser.getExtensionFilters().add(new ExtensionFilter("Legal document config", "*.serl_DOCS"));

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
                Alert alert = new Alert(AlertType.NONE);

                alert.setTitle("Configuration exported");
                alert.setHeaderText("Configuration exported to desktop.");

                ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);

                alert.getButtonTypes().setAll(okButton);
                alert.showAndWait();
            }
            else
            {
                Alert alert = new Alert(AlertType.ERROR);

                alert.setTitle("Configuration not exported");
                alert.setHeaderText("An error occurred while exporting configuration. Please contact me at [email].");

                ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);

                alert.getButtonTypes().setAll(okButton);
                alert.showAndWait();
            }
        });
    }

    private void addDocsToManager(List<Document> docs)
    {
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
                double step = 1.0 / (double) docs.size();
                double progress = 0.0;

                root.setCursor(Cursor.WAIT);

                for (Document doc : docs)
                {
                    indexingProgressBar.setProgress(progress);

                    this.dm.addDocument(doc);

                    if (!DocumentMatcher.hasSerialisedTrieOf(doc))
                    {
                        DocumentMatcher.serialiseTrieOf(doc);
                    }

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

    private void initAddDoc()
    {
        this.addDocButton.setOnAction(e ->
        {
            FileChooser docFileChooser = new FileChooser();
            docFileChooser.setTitle("Select file");

            File selected = docFileChooser.showOpenDialog(this.getStage());

            if (selected != null)
            {
                Document docToAdd = new Document(selected);

                this.displayDetailsDialog(docToAdd);

                this.dm.addDocument(docToAdd);
                DocumentMatcher.serialiseTrieOf(docToAdd);
            }

            this.refreshDocsDetails();
        });
    }

    private void initAddDir()
    {
        this.addDirButton.setOnAction(e ->
        {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select folder");

            File selected = dirChooser.showDialog(this.getStage());
            Path dirToAdd = selected.toPath();

            List<Document> docsToAdd = new LinkedList<>();

            try
            {
                Files.walk(dirToAdd).filter(Files::isRegularFile).forEach(f -> docsToAdd.add(new Document(f.toFile())));

                this.addDocsToManager(docsToAdd);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        });
    }

    private void displayDetailsDialog(Document selected)
    {
        DetailsController detailsController = new DetailsController(this, selected, this.dm);

        FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("detailsView.fxml"));
        loader.setController(detailsController);

        try
        {
            Parent root = loader.load();

            Scene dialogScene = new Scene(root);
            Stage dialogStage = new Stage();

            dialogStage.setScene(dialogScene);
            dialogStage.setTitle("Document details");

            dialogStage.showAndWait();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void refreshFilteredDocs()
    {
        List<Document> displayedDocs = this.getDisplayedDocs();

        this.docsList = FXCollections.observableArrayList(this.dm.listDocuments());
        this.docsFiltered = new FilteredList<>(this.docsList, displayedDocs::contains);

        this.docTableView.setItems(this.docsFiltered);
    }

    public void refreshDocsDetails()
    {
        this.docsList.clear();

        this.docsList = FXCollections.observableArrayList(this.dm.listDocuments());
        this.docsFiltered = new FilteredList<>(this.docsList, x -> true);

        this.docTableView.setItems(this.docsFiltered);
    }

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
    }
}
