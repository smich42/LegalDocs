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
import javafx.scene.image.Image;
import javafx.stage.Stage;
import legal.LCase;
import legal.LClient;
import legal.LCourt;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import document.*;

public class Main extends Application
{
    private static final DocumentManager dm = new DocumentManager();
    private ObservableList<Document> docsList = FXCollections.observableArrayList(dm.listDocuments());
    private FilteredList<Document> docsFiltered = new FilteredList(docsList, x -> true);

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

    public void fillFileListView(Scene scene)
    {
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

    public void initFilter(Scene scene)
    {
        ChoiceBox<String> cb = (ChoiceBox<String>) scene.lookup("#filterChoiceBox");
        cb.getItems().addAll("Name", "Case", "Client", "Court");
        cb.setValue("Name");


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

    public void initSearch(Scene scene)
    {
        TextField tf = (TextField) scene.lookup("#searchTextField");
        Button bt = (Button) scene.lookup("#searchButton");

        bt.setOnAction(e -> {

            scene.setCursor(Cursor.WAIT);

            // Using Roland's answer at https://stackoverflow.com/a/28206116/7970195 for threading
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String S = tf.getText().trim();

                        if (S.isEmpty() || S.isBlank())
                        {
                            docsFiltered.setPredicate(x -> true);
                        }
                        else
                        {
                            List<Document> matches = dm.searchExactly(S);

                            for (Document doc : matches)
                            {
                                System.out.println(doc);
                            }

                            docsFiltered.setPredicate(x -> matches.contains(x));
                        }
                    }
                    catch (Throwable th)
                    {
                        th.printStackTrace();
                    }
                    finally
                    {
                        tf.setText("");
                        Platform.runLater(() -> {
                            scene.setCursor(Cursor.DEFAULT);
                        });
                    }
                }
            });

            thread.start();
        });
    }

    public void initSort(Scene scene)
    {
        ChoiceBox<String> cb = (ChoiceBox<String>) scene.lookup("#sortChoiceBox");
        cb.getItems().addAll("Name", "Case", "Client", "Court", "Date");
        cb.setValue("Name");

        Button bt = (Button) scene.lookup("#sortButton");

        bt.setOnAction(e -> {
            switch (cb.getValue())
            {
                case "Name":
                    dm.sortByCategory(Document.class);
                    break;
                case "Case":
                    dm.sortByCategory(LCase.class);
                    break;
                case "Client":
                    dm.sortByCategory(LClient.class);
                    break;
                case "Court":
                    dm.sortByCategory(LCourt.class);
                    break;
                case "Date":
                    dm.sortByCategory(Date.class);
                    break;
                default:
                    break;
            }

            List<Document> displayedDocs = getDisplayedDocs();

            docsList = FXCollections.observableArrayList(dm.listDocuments());
            docsFiltered = new FilteredList(docsList, x -> displayedDocs.contains(x));

            tv.setItems(docsFiltered);
        });
    }

    public void initDelete(Scene scene)
    {
        Button bt = (Button) scene.lookup("#deleteButton");

        bt.setOnAction(e -> {
            Document selected = tv.getSelectionModel().getSelectedItem();

            if (selected != null)
            {
                Alert alert = new Alert(AlertType.CONFIRMATION);

                alert.setTitle("Delete file");
                alert.setHeaderText("Choose deletion type");

                ButtonType deleteFileButton = new ButtonType("Delete file");
                ButtonType deleteDocumentKeepFileButton = new ButtonType("Delete document and keep file");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(deleteFileButton, deleteDocumentKeepFileButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == deleteFileButton)
                {
                    dm.deleteDocumentAndFile(selected);
                }
                else if (result.get() == deleteDocumentKeepFileButton)
                {
                    dm.deleteDocument(selected);
                }

                List<Document> displayedDocs = getDisplayedDocs();

                docsList = FXCollections.observableArrayList(dm.listDocuments());
                docsFiltered = new FilteredList(docsList, x -> displayedDocs.contains(x));

                tv.setItems(docsFiltered);
            }
        });
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

        fillFileListView(scene);

        initFilter(scene);
        initSearch(scene);
        initSort(scene);
        initDelete(scene);
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
