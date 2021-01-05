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

    private Stage stage;
    private String name;

    public String getName()
    {
        return (this.name.isBlank() ? null : this.name);
    }

    private Stage getStage()
    {
        return (Stage) this.cancelButton.getScene().getWindow();
    }

    public AskNameController()
    {
        this.name = "";
    }

    @Override
    public void initialize(URL url, ResourceBundle resources)
    {
        createButton.setDisable(true);

        nameTextField.textProperty().addListener((obsVal, oldText, newText) -> createButton.setDisable(newText.isBlank()));

        createButton.setOnAction(e -> {
            this.name = nameTextField.getText();
            this.getStage().close();
        });

        cancelButton.setOnAction(e -> this.getStage().close());
    }
}
