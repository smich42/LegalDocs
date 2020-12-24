package gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import legal.LCase;
import legal.LClient;
import legal.LCourt;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ResourceBundle;
import document.Document;
import document.DocumentManager;

public class DetailsController implements Initializable
{
    private Document doc;
    private DocumentManager dm;

    @FXML
    private TextField docNameTextField;

    @FXML
    private ChoiceBox<LCase> caseChoiceBox;

    @FXML
    private TextField caseNameTextField;

    @FXML
    private Button newCaseButton;

    @FXML
    private ChoiceBox<LClient> clientChoiceBox;

    @FXML
    private Button newClientButton;

    @FXML
    private TextField clientNameTextField;

    @FXML
    private TextField clientEmailTextField;

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

    public void initChoiceBoxes()
    {
        this.caseChoiceBox.getItems().addAll(dm.listCases());
        this.clientChoiceBox.getItems().addAll(dm.listClients());
        this.courtChoiceBox.getItems().addAll(dm.listCourts());

        this.courtTypeChoiceBox.getItems().addAll(LCourt.CourtTypes.values());
    }

    public void fillDocumentFields(Document doc)
    {
        this.docNameTextField.setText(doc.getName());

        this.fillCaseFields(doc.getCase());
    }

    public void fillCaseFields(LCase lCase)
    {
        this.caseChoiceBox.setValue(doc.getCase());
        this.caseNameTextField.setText(doc.getCase().getName());

        Instant dateAssignedInstant = Instant.ofEpochMilli(doc.getDateAssigned().getTime());

        this.dateAssignedDatePicker.setValue(dateAssignedInstant.atZone(ZoneId.systemDefault()).toLocalDate());

        this.fillClientValues(lCase.getClient());
        this.fillCourtValues(lCase.getCourt());
    }

    public void fillClientValues(LClient lClient)
    {
        this.clientChoiceBox.setValue(lClient);

        this.clientNameTextField.setText(lClient.getName());
        this.clientEmailTextField.setText(lClient.getEmail());
        this.clientPhoneTextField.setText(lClient.getPhone());
    }

    public void fillCourtValues(LCourt lCourt)
    {
        this.courtChoiceBox.setValue(lCourt);

        this.courtNameTextField.setText(lCourt.getName());
        this.courtTypeChoiceBox.setValue(lCourt.getType());
    }

    public void updateDetails()
    {

    }

    public DetailsController(Document doc, DocumentManager dm)
    {
        this.dm = dm;
        this.doc = doc;
    }

    @Override
    public void initialize(URL url, ResourceBundle resources)
    {
        this.initChoiceBoxes();

        this.fillDocumentFields(doc);
    }
}
