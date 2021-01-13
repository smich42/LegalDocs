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

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        // Create instance of main controller
        MainController mainController = new MainController(dm);

        // Load main view, setting controller
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("mainView.fxml"));
        loader.setController(mainController);

        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 640);

        stage.setTitle("Legal document browser");
        stage.getIcons().add(new Image("logo.png"));
        stage.getProperties().put("hostServices", this.getHostServices());

        // Display main scene
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception
    {
        // Use DocumentManager::close to exit gracefully
        dm.close();
        super.stop();
    }
}
