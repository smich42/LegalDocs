package gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import legal.LCase;
import legal.LClient;
import legal.LCourt;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import document.Document;
import document.DocumentManager;

public class DetailsController implements Initializable
{
    private MainController mainController;
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

    public LocalDate toLocalDate(Date date)
    {
        Instant dateInstant = date.toInstant();
        return dateInstant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public Date fromLocalDate(LocalDate localDate)
    {
        Instant dateInstant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(dateInstant);
    }

    public Stage getStage()
    {
        return (Stage) this.cancelButton.getScene().getWindow();
    }

    public void initChoiceBoxes()
    {
        this.caseChoiceBox.getItems().addAll(dm.listCases());
        this.clientChoiceBox.getItems().addAll(dm.listClients());
        this.courtChoiceBox.getItems().addAll(dm.listCourts());

        this.courtTypeChoiceBox.getItems().addAll(LCourt.CourtTypes.values());

        // Using zhujik's answer at https://stackoverflow.com/a/14523434/7970195
        caseChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            LCase selected = caseChoiceBox.getItems().get((Integer) number2);
            fillCaseFields(selected);
        });

        clientChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            LClient selected = clientChoiceBox.getItems().get((Integer) number2);
            fillClientValues(selected);
        });

        courtChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            LCourt selected = courtChoiceBox.getItems().get((Integer) number2);
            fillCourtValues(selected);
        });
    }

    public void fillDocumentFields(Document doc)
    {
        this.docNameTextField.setText(doc.getName());

        this.fillCaseFields(doc.getCase());
    }

    public void fillCaseFields(LCase lCase)
    {
        this.caseChoiceBox.setValue(lCase);
        this.caseNameTextField.setText(lCase.getName());

        this.dateAssignedDatePicker.setValue(this.toLocalDate(lCase.getDateAssigned()));

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

    public void saveChanges()
    {
        LCase lCase = this.caseChoiceBox.getValue();
        LClient lClient = this.clientChoiceBox.getValue();
        LCourt lCourt = this.courtChoiceBox.getValue();

        lClient.setName(this.clientNameTextField.getText());
        lClient.setEmail(this.clientEmailTextField.getText());
        lClient.setPhone(this.clientPhoneTextField.getText());

        lCourt.setType(this.courtTypeChoiceBox.getValue());
        lCourt.setName(this.courtNameTextField.getText());

        lCase.setName(this.caseNameTextField.getText());
        lCase.setDateAssigned(this.fromLocalDate(dateAssignedDatePicker.getValue()));

        this.doc.setName(this.docNameTextField.getText());

        for (Document curDoc : dm.listDocuments())
        {
            if (curDoc.getClient() == this.clientChoiceBox.getValue())
            {
                lCase.setClient(lClient);
            }

            if (curDoc.getCourt() == this.courtChoiceBox.getValue())
            {
                lCase.setCourt(lCourt);
            }

            if (curDoc.getCase() == this.caseChoiceBox.getValue())
            {
                curDoc.setCase(lCase);
            }
        }
    }

    public DetailsController(MainController mainController, Document doc, DocumentManager dm)
    {
        this.mainController = mainController;
        this.dm = dm;
        this.doc = doc;
    }

    @Override
    public void initialize(URL url, ResourceBundle resources)
    {
        this.initChoiceBoxes();
        this.fillDocumentFields(doc);

        this.cancelButton.setOnAction(e -> this.getStage().close());
        this.confirmationButton.setOnAction(e -> {
            this.saveChanges();
            this.mainController.refreshDocsDetails();
            this.getStage().close();
        });
    }
}
