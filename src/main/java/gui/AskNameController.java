package gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    public String getName()
    {
        return (this.name.isBlank() ? null : this.name);
    }

    private Stage getStage()
    {
        return (Stage) this.cancelButton.getScene().getWindow();
    }

    @Override
    public void initialize(URL url, ResourceBundle resources)
    {
        this.createButton.setDisable(true);

        this.nameTextField.textProperty().addListener((obsVal, oldText, newText) -> this.createButton.setDisable(newText.isBlank()));

        this.createButton.setOnAction(e ->
        {
            this.name = this.nameTextField.getText();
            this.getStage().close();
        });

        this.cancelButton.setOnAction(e -> this.getStage().close());
    }
}
