package gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import legal.LCase;
import legal.LCourt;
import java.net.URL;
import java.util.ResourceBundle;
import document.Document;

public class DetailsController implements Initializable
{
    @FXML
    private TextField nameTextField;

    @FXML
    private ChoiceBox<LCase> caseChoiceBox;

    @FXML
    private Button newCaseButton;

    @FXML
    private ChoiceBox<LCase> clientChoiceBox;

    @FXML
    private Button newClientButton;

    @FXML
    private TextField clientNameTextField;

    @FXML
    private TextField clientEmaiTextField;

    @FXML
    private TextField clientPhoneTextField;

    @FXML
    private ChoiceBox<LCourt> courtChoiceBox;

    @FXML
    private Button newCourtButton;

    @FXML
    private ChoiceBox<LCourt.CourtTypes> courtTypeChoiceBox;

    @FXML
    private TextField courtNameTextField;

    @FXML
    private DatePicker dateAssignedDatePicker;

    @FXML
    private Button confirmationButton;

    @FXML
    private Button cancelButton;


    public DetailsController(Document doc)
    {
    }

    @Override
    public void initialize(URL url, ResourceBundle resources)
    {
    }
}
