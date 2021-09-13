/*
 * IMPORTS
 * document.DocumentManager: Main class starts a DocumentManager instance for the entire application
 * package gui: Holds GUI controllers
 * package javafx.application: For basic operations on the JavaFX app
 * package javafx.fxml: For making use of FXML to create views
 * package javafx.scene: For basic operations on JavaFX graphics
 */

import document.DocumentManager;
import gui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/*
 * RESPONSIBILITIES
 * - Application startup.
 * - Shows main view.
 * - Opens and closes DocumentManager instance.
 */

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

        // Load main view and set controller
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("mainView.fxml"));
        loader.setController(mainController);

        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 640);

        // Set stage properties
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
