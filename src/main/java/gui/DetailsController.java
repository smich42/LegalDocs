package gui;

import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import legal.LCase;
import legal.LClient;
import legal.LCourt;
import java.io.IOException;
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
    private Text invalidWarning;

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

    private static LocalDate toLocalDate(Date date)
    {
        Instant dateInstant = date.toInstant();
        return dateInstant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date fromLocalDate(LocalDate localDate)
    {
        Instant dateInstant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(dateInstant);
    }

    private Stage getStage()
    {
        return (Stage) this.cancelButton.getScene().getWindow();
    }

    private void initChoiceBoxes()
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
            fillClientFields(selected);
        });

        courtChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            LCourt selected = courtChoiceBox.getItems().get((Integer) number2);
            fillCourtFields(selected);
        });
    }

    private void fillDocumentFields(Document doc)
    {
        this.docNameTextField.setText(doc.getName());

        this.fillCaseFields(doc.getCase());
    }

    private void fillCaseFields(LCase lCase)
    {
        this.caseChoiceBox.setValue(lCase);
        this.caseNameTextField.setText(lCase.getName());

        this.dateAssignedDatePicker.setValue(toLocalDate(lCase.getDateAssigned()));

        this.fillClientFields(lCase.getClient());
        this.fillCourtFields(lCase.getCourt());
    }

    private void fillClientFields(LClient lClient)
    {
        this.clientChoiceBox.setValue(lClient);

        this.clientNameTextField.setText(lClient.getName());
        this.clientEmailTextField.setText(lClient.getEmail());
        this.clientPhoneTextField.setText(lClient.getPhone());
    }

    private void fillCourtFields(LCourt lCourt)
    {
        this.courtChoiceBox.setValue(lCourt);

        this.courtNameTextField.setText(lCourt.getName());
        this.courtTypeChoiceBox.setValue(lCourt.getType());
    }

    private void saveChanges()
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
        lCase.setDateAssigned(fromLocalDate(dateAssignedDatePicker.getValue()));
        lCase.setClient(lClient);
        lCase.setCourt(lCourt);

        this.doc.setName(this.docNameTextField.getText());

        for (Document curDoc : dm.listDocuments())
        {
            if (curDoc == this.doc)
            {
                // Always modify the details of the current document because a new LCase/LClient/LCourt may have been created by the user
                // in which case the comparisons below will (and should) fail
                this.doc.setCase(lCase);
            }

            if (curDoc.getClient() == this.clientChoiceBox.getValue())
            {
                curDoc.getCase().setClient(lClient);
            }

            if (curDoc.getCourt() == this.courtChoiceBox.getValue())
            {
                curDoc.getCase().setCourt(lCourt);
            }

            if (curDoc.getCase() == this.caseChoiceBox.getValue())
            {
                curDoc.setCase(lCase);
            }
        }
    }

    private void initNewButtons()
    {
        this.newCaseButton.setOnAction(e -> {
            String name = this.askName();

            if (name != null)
            {
                LCase newCase = new LCase(name, this.courtChoiceBox.getValue(), this.clientChoiceBox.getValue(),
                        fromLocalDate(this.dateAssignedDatePicker.getValue()));

                dm.addCase(newCase);

                this.caseChoiceBox.getItems().add(newCase);
                this.caseChoiceBox.setValue(newCase);

                this.fillCaseFields(newCase);
            }
        });

        this.newClientButton.setOnAction(e -> {
            String name = this.askName();

            if (name != null)
            {
                LClient newClient = new LClient(name);

                dm.addClient(newClient);

                this.clientChoiceBox.getItems().add(newClient);
                this.clientChoiceBox.setValue(newClient);

                this.fillClientFields(newClient);
            }
        });

        this.newCourtButton.setOnAction(e -> {
            String name = this.askName();

            if (name != null)
            {
                LCourt newCourt = new LCourt(name);

                dm.addCourt(newCourt);

                this.courtChoiceBox.getItems().add(newCourt);
                this.courtChoiceBox.setValue(newCourt);

                this.fillCourtFields(newCourt);
            }
        });
    }

    private String askName()
    {
        AskNameController askNameController = new AskNameController();

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("askNameView.fxml"));
        loader.setController(askNameController);

        try
        {
            Parent root = loader.load();

            Scene askNameScene = new Scene(root);
            Stage askNameStage = new Stage();

            askNameStage.setScene(askNameScene);
            askNameStage.setTitle("Enter name");

            askNameStage.showAndWait();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return askNameController.getName();
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
        this.initNewButtons();
        this.fillDocumentFields(doc);

        this.cancelButton.setOnAction(e -> this.getStage().close());

        this.confirmationButton.setOnAction(e -> {
            this.saveChanges();
            this.mainController.refreshDocsDetails();
            this.getStage().close();
        });

        // Using Uluk Biy's answer at https://stackoverflow.com/a/23041348/7970195
        BooleanBinding dataIsInvalid = new BooleanBinding()
        {
            {
                super.bind(clientEmailTextField.textProperty(), clientPhoneTextField.textProperty());
            }

            @Override
            protected boolean computeValue()
            {
                return !LClient.validateEmail(clientEmailTextField.getText()) || !LClient.validatePhone(clientPhoneTextField.getText());
            }
        };

        this.invalidWarning.visibleProperty().bind(dataIsInvalid);
        this.confirmationButton.disableProperty().bind(dataIsInvalid);
    }
}
