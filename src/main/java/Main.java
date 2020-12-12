import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import document.*;

public class Main extends Application
{
    public void fillFileVBox(Scene scene)
    {
        VBox vb = (VBox) scene.lookup("#FileVBox");

        DocumentManager dm = new DocumentManager();
        Test.addWikiDocuments(dm);

        for (Document doc : dm.listDocuments())
        {
            vb.getChildren().add(new Text(doc.getName()));
            System.out.println("Added document '" + doc.getName() + "' to VBox");
        }

        dm.close();
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("test.fxml"));
        Scene scene = new Scene(root, 640, 480);

        stage.setTitle("FXML Welcome");
        stage.setScene(scene);

        stage.show();

        fillFileVBox(scene);
    }

    public static void main(String[] args)
    {
        mergeErrOutStreams();

        launch(args);
    }

    public static void mergeErrOutStreams()
    {
        System.err.close();
        System.setErr(System.out);
    }
}
