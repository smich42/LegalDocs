import document.DocumentManager;
import gui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application
{
    private static final DocumentManager dm = new DocumentManager();

    @Override
    public void start(Stage stage) throws Exception
    {
        MainController mainController = new MainController(dm);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainView.fxml"));
        loader.setController(mainController);

        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 640);

        stage.setTitle("Legal document browser");
        stage.getIcons().add(new Image("logo.png"));
        stage.getProperties().put("hostServices", this.getHostServices());

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception
    {
        dm.close();
        super.stop();
    }

    public static void main(String[] args)
    {
        Utilities.addWikiDocuments(dm);
        launch(args);
    }
}
