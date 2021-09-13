package gui;

/*
 * IMPORTS
 * package javafx.fxml: For making use of FXML to create views
 * package javafx.scene: For basic operations on JavaFX graphics
 * java.util.ResourceBundle: Used by JavaFX controllers for resources dependent on locale
 * java.net.URL: For default FXML binding used by JavaFX initialisers
 */

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/*
 * RESPONSIBILITIES
 * - Controls view asking for a name
 * - Initialises JavaFX controls
 */

public class AskNameController implements Initializable
{
    @FXML
    private TextField nameTextField;

    @FXML
    private Button createButton;

    @FXML
    private Button cancelButton;

    private String name;

    public AskNameController()
    {
        this.name = "";
    }

    /* Returns name confirmed by user */
    public String getName()
    {
        return (this.name.isBlank() ? null : this.name);
    }

    /* Returns main stage of this AskName view */
    private Stage getStage()
    {
        return (Stage) this.cancelButton.getScene().getWindow();
    }

    @Override
    public void initialize(URL url, ResourceBundle resources)
    {
        // Catch change in TextField to enable or disable confirmation button
        // depending on whether
        this.createButton.setDisable(true);
        this.nameTextField.textProperty().addListener((obsVal, oldText, newText) -> this.createButton.setDisable(newText.isBlank()));

        this.createButton.setOnAction(e ->
        {
            // Update name only if confirmation button is clicked
            this.name = this.nameTextField.getText();
            this.getStage().close();
        });

        this.cancelButton.setOnAction(e -> this.getStage().close());
    }
}
