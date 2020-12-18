import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import document.Document;
import document.DocumentManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import legal.LCase;
import legal.LClient;
import legal.LCourt;

public class Main extends Application
{
    private static final DocumentManager dm = new DocumentManager();
    private ObservableList<Document> docsList = FXCollections.observableArrayList(dm.listDocuments());
    private FilteredList<Document> docsFiltered = new FilteredList<>(docsList, x -> true);

    private TableView<Document> tv;

    public List<Document> getDisplayedDocs()
    {
        List<Document> displayedDocs = new ArrayList<>();

        for (Document doc : docsFiltered)
        {
            displayedDocs.add(doc);
        }

        return displayedDocs;
    }

    public void initTableView(Stage stage)
    {
        Scene scene = stage.getScene();

        tv = (TableView<Document>) scene.lookup("#fileTable");

        // Based on James_D's answer at https://stackoverflow.com/a/26565887/7970195
        tv.setRowFactory(table -> {
            TableRow<Document> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty())
                {
                    getHostServices().showDocument(row.getItem().getFullPath()); // Open file with defaut programme
                }
            });

            return row;
        });

        Button deleteBt = (Button) scene.lookup("#deleteButton");
        Button editBt = (Button) scene.lookup("#detailsButton");

        tv.setOnMouseClicked(e -> {
            deleteBt.setDisable(false);
            editBt.setDisable(false);
        });

        TableColumn<Document, String> nameCol = (TableColumn<Document, String>) tv.getColumns().get(0);
        TableColumn<Document, String> caseCol = (TableColumn<Document, String>) tv.getColumns().get(1);
        TableColumn<Document, String> clientCol = (TableColumn<Document, String>) tv.getColumns().get(2);
        TableColumn<Document, String> courtCol = (TableColumn<Document, String>) tv.getColumns().get(3);
        TableColumn<Document, String> dateCol = (TableColumn<Document, String>) tv.getColumns().get(4);

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        caseCol.setCellValueFactory(new PropertyValueFactory<>("case"));
        clientCol.setCellValueFactory(new PropertyValueFactory<>("client"));
        courtCol.setCellValueFactory(new PropertyValueFactory<>("court"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateAssigned"));

        tv.setItems(docsFiltered);
    }

    public void initFilter(Stage stage)
    {
        Scene scene = stage.getScene();

        ChoiceBox<String> cb = (ChoiceBox<String>) scene.lookup("#filterChoiceBox");
        cb.getItems().addAll("Name", "Case", "Client", "Court");
        cb.setValue("Name");

        // java(16777761)
        TextField tf = (TextField) scene.lookup("#filterTextField");

        tf.setOnKeyReleased(e -> {
            switch (cb.getValue())
            {
                case "Name":
                    docsFiltered.setPredicate(x -> x.getName().toLowerCase().contains(tf.getText().toLowerCase()));
                    break;
                case "Case":
                    docsFiltered.setPredicate(x -> x.getCase().getName().toLowerCase().contains(tf.getText().toLowerCase()));
                    break;
                case "Client":
                    docsFiltered.setPredicate(x -> x.getClient().getName().toLowerCase().contains(tf.getText().toLowerCase()));
                    break;
                case "Court":
                    docsFiltered.setPredicate(x -> x.getCourt().getName().toLowerCase().contains(tf.getText().toLowerCase()));
                    break;
                default:
                    break;
            }
        });

        cb.getSelectionModel().selectedItemProperty().addListener((val, prev, next) -> {
            tf.setText("");
            docsFiltered.setPredicate(x -> true);
        });
    }

    public void initSearch(Stage stage)
    {
        Scene scene = stage.getScene();

        TextField tf = (TextField) scene.lookup("#searchTextField");
        Button bt = (Button) scene.lookup("#searchButton");

        bt.setOnAction(e -> {

            scene.setCursor(Cursor.WAIT);

            // Using Roland's answer at https://stackoverflow.com/a/28206116/7970195 for threading
            Thread thread = new Thread(() -> {
                try
                {
                    String S = tf.getText().trim();

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

                        docsFiltered.setPredicate(x2 -> matches.contains(x2));
                    }
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
                finally
                {
                    tf.setText("");
                    Platform.runLater(() -> scene.setCursor(Cursor.DEFAULT));
                }
            });

            thread.start();
        });
    }

    public void initSort(Stage stage)
    {
        Scene scene = stage.getScene();

        ChoiceBox<String> cb = (ChoiceBox<String>) scene.lookup("#sortChoiceBox");
        cb.getItems().addAll("Name sorting", "Case sorting", "Client sorting", "Court sorting", "Date sorting");
        cb.setValue("Previous sorting");

        cb.setOnAction(e -> {
            switch (cb.getValue())
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

    public void initDelete(Stage stage)
    {
        Scene scene = stage.getScene();

        Button bt = (Button) scene.lookup("#deleteButton");

        bt.setDisable(true);

        bt.setOnAction(e -> {
            Document selected = tv.getSelectionModel().getSelectedItem();

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

    public void initDetails(Stage stage)
    {
        Scene scene = stage.getScene();

        Button bt = (Button) scene.lookup("#detailsButton");

        bt.setDisable(true);

        bt.setOnAction(e -> {
            Document selected = tv.getSelectionModel().getSelectedItem();

            if (selected != null)
            {
                displayDetailsDialog(stage, selected);

                refreshFilteredDocs();
            }
        });
    }

    public void displayDetailsDialog(Stage owner, Document selected)
    {
        System.out.println("Details");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("detailsView.fxml"));

        Parent root;
        try
        {
            root = (Parent) loader.load();

            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setScene(scene);
            stage.setTitle("Document details");

            stage.show();
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
        docsFiltered = new FilteredList<>(docsList, x -> displayedDocs.contains(x));

        tv.setItems(docsFiltered);
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("mainView.fxml"));
        Scene scene = new Scene(root, 900, 640);

        stage.setTitle("Legal document browser");
        stage.getIcons().add(new Image("logo.png"));

        stage.setScene(scene);
        stage.show();

        initTableView(stage);

        initFilter(stage);
        initSearch(stage);
        initSort(stage);
        initDelete(stage);
        initDetails(stage);
    }

    @Override
    public void stop() throws Exception
    {
        dm.close();
        super.stop();
    }

    public static void main(String[] args)
    {
        Test.addWikiDocuments(dm);
        launch(args);
    }
}
