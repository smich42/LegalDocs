package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import document.Document;
import document.DocumentManager;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import legal.LCase;
import legal.LClient;
import legal.LCourt;

public class MainController implements Initializable
{
    private DocumentManager dm;
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

    public Stage getStage()
    {
        return (Stage) this.mainAnchorPane.getScene().getWindow();
    }

    public List<Document> getDisplayedDocs()
    {
        List<Document> displayedDocs = new ArrayList<>();

        for (Document doc : docsFiltered)
        {
            displayedDocs.add(doc);
        }

        return displayedDocs;
    }

    public void initTableView()
    {
        // Based on James_D's answer at https://stackoverflow.com/a/26565887/7970195
        this.docTableView.setRowFactory(table -> {
            TableRow<Document> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty())
                {

                    HostServices hostServices = (HostServices) this.getStage().getProperties().get("hostServices");

                    hostServices.showDocument(row.getItem().getFullPath()); // Open file with defaut programme
                }
            });

            return row;
        });

        this.docTableView.setOnMouseClicked(e -> {
            deleteButton.setDisable(false);
            detailsButton.setDisable(false);
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

        this.docTableView.setItems(docsFiltered);
    }

    public void initFilter()
    {
        filterChoiceBox.getItems().addAll("Name", "Case", "Client", "Court");
        filterChoiceBox.setValue("Name");

        filterTextField.setOnKeyReleased(e -> {
            switch (filterChoiceBox.getValue())
            {
                case "Name":
                    docsFiltered.setPredicate(x -> x.getName().toLowerCase().contains(filterTextField.getText().toLowerCase()));
                    break;

                case "Case":
                    docsFiltered.setPredicate(x -> x.getCase().getName().toLowerCase().contains(filterTextField.getText().toLowerCase()));
                    break;

                case "Client":
                    docsFiltered.setPredicate(x -> x.getClient().getName().toLowerCase().contains(filterTextField.getText().toLowerCase()));
                    break;

                case "Court":
                    docsFiltered.setPredicate(x -> x.getCourt().getName().toLowerCase().contains(filterTextField.getText().toLowerCase()));
                    break;

                default:
                    break;
            }
        });

        filterChoiceBox.getSelectionModel().selectedItemProperty().addListener((val, prev, next) -> {
            filterTextField.setText("");
            docsFiltered.setPredicate(x -> true);
        });
    }

    public void initSearch()
    {
        searchButton.setOnAction(e -> {

            Parent root = mainAnchorPane.getScene().getRoot();

            root.setCursor(Cursor.WAIT);

            // Using Roland's answer at https://stackoverflow.com/a/28206116/7970195 for threading
            Thread thread = new Thread(() -> {
                try
                {
                    String S = searchTextField.getText().trim();

                    if (S.isEmpty() || S.isBlank())
                    {
                        docsFiltered.setPredicate(x1 -> true);
                    }
                    else
                    {
                        List<Document> matches = dm.searchExactly(S);

                        for (Document doc : matches)
                        {
                            System.out.println(doc);
                        }

                        docsFiltered.setPredicate(matches::contains);
                    }
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
                finally
                {
                    Platform.runLater(() -> searchTextField.setText(""));
                    Platform.runLater(() -> root.setCursor(Cursor.DEFAULT));
                }
            });

            thread.start();
        });
    }

    public void initSort()
    {
        sortChoiceBox.getItems().addAll("Name sorting", "Case sorting", "Client sorting", "Court sorting", "Date sorting");
        sortChoiceBox.setValue("Previous sorting");

        sortChoiceBox.setOnAction(e -> {
            switch (sortChoiceBox.getValue())
            {
                case "Name sorting":
                    dm.sortByCategory(Document.class);
                    break;

                case "Case sorting":
                    dm.sortByCategory(LCase.class);
                    break;

                case "Client sorting":
                    dm.sortByCategory(LClient.class);
                    break;

                case "Court sorting":
                    dm.sortByCategory(LCourt.class);
                    break;

                case "Date sorting":
                    dm.sortByCategory(Date.class);
                    break;

                default:
                    break;
            }

            refreshFilteredDocs();
        });
    }

    public void initDelete()
    {
        deleteButton.setDisable(true);

        deleteButton.setOnAction(e -> {
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

                if (result.get() == deleteFileButton)
                {
                    dm.deleteDocumentAndFile(selected);
                }
                else if (result.get() == deleteDocumentKeepFileButton)
                {
                    dm.deleteDocument(selected);
                }

                refreshFilteredDocs();
            }
        });
    }

    public void initDetails()
    {
        detailsButton.setDisable(true);

        detailsButton.setOnAction(e -> {
            Document selected = this.docTableView.getSelectionModel().getSelectedItem();

            if (selected != null)
            {
                displayDetailsDialog(selected);

                refreshFilteredDocs();
            }
        });
    }

    public void initImportExport()
    {
        importButton.setOnAction(e -> {
            FileChooser importFileChooser = new FileChooser();
            importFileChooser.setTitle("Select import file");

            importFileChooser.getExtensionFilters().add(new ExtensionFilter("Legal document configuration files", "*.serl_DOCS"));

            File selected = importFileChooser.showOpenDialog(this.getStage());

            if (selected != null)
            {
                dm.importSerialised(selected);
            }

            this.refreshDocsDetails();
        });

        exportButton.setOnAction(e -> {
            if (dm.exportSerialised())
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

    public void displayDetailsDialog(Document selected)
    {
        DetailsController detailsController = new DetailsController(this, selected, this.dm);

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("detailsView.fxml"));
        loader.setController(detailsController);

        try
        {
            Parent root = loader.load();

            Scene dialogScene = new Scene(root);
            Stage dialogStage = new Stage();

            dialogStage.setScene(dialogScene);
            dialogStage.setTitle("Document details");

            dialogStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void refreshFilteredDocs()
    {
        List<Document> displayedDocs = getDisplayedDocs();

        docsList = FXCollections.observableArrayList(dm.listDocuments());
        docsFiltered = new FilteredList<>(docsList, displayedDocs::contains);

        this.docTableView.setItems(docsFiltered);
    }

    public void refreshDocsDetails()
    {
        docsList.clear();

        docsList = FXCollections.observableArrayList(dm.listDocuments());
        docsFiltered = new FilteredList<>(docsList, x -> true);

        this.docTableView.setItems(docsFiltered);
    }

    public MainController(DocumentManager dm)
    {
        this.dm = dm;

        this.docsList = FXCollections.observableArrayList(dm.listDocuments());
        this.docsFiltered = new FilteredList<>(docsList, x -> true);
    }

    @Override
    public void initialize(URL url, ResourceBundle resources)
    {
        initTableView();

        initFilter();
        initSearch();
        initSort();
        initDelete();
        initDetails();
        initImportExport();
    }
}
