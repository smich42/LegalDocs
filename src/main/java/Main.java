import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import document.*;

public class Main extends Application
{
    private static final DocumentManager dm = new DocumentManager();

    public void fillFileListView(Scene scene)
    {
        TableView<Document> tv = (TableView<Document>) scene.lookup("#fileTable");

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

        Test.addWikiDocuments(dm);

        for (Document doc : dm.listDocuments())
        {
            tv.getItems().add(doc);
            System.out.println("Added document '" + doc.getName() + "' to ListView");
        }
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("mainView.fxml"));
        Scene scene = new Scene(root, 800, 640);

        stage.setTitle("Legal document browser");
        stage.getIcons().add(new Image("logo.png"));

        stage.setScene(scene);

        stage.show();

        fillFileListView(scene);
    }

    public static void main(String[] args)
    {
        launch(args);
        dm.close();
    }
}
