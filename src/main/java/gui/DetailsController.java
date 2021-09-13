package gui;

/*
 * IMPORTS
 * package document: Used for document handling
 * javafx.beans.binding.BooleanBinding: For binding properties of GUI elements to a binary condition
 * package javafx.fxml: For making use of FXML to create views
 * package javafx.scene: For basic operations on JavaFX graphics
 * package javafx.stage: For operations on the containers containing JavaFX controls, etc.
 * package legal: For classes representing cases, clients and courts
 * java.io.IOException: To handle errors in I/O operations
 * package java.time: Used for operations relating to time/date
 * java.util.Date: Represents assignment date
 * java.util.ResourceBundle: Used by JavaFX controllers for resources dependent on locale
 * java.net.URL: For default FXML binding used by JavaFX initialisers
 */

import document.Document;
import document.DocumentManager;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

/*
 * RESPONSIBILITIES
 * - Controls details view.
 * - Initialises JavaFX controls.
 */

public class DetailsController implements Initializable
{
    // Main controller that owns this DetailsController
    private final MainController mainController;

    private final Document doc;
    private final DocumentManager dm;

    /* FXML bindings */
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

    public DetailsController(MainController mainController, Document doc, DocumentManager dm)
    {
        this.mainController = mainController;
        this.dm = dm;
        this.doc = doc;
    }

    /*
     * Converts java.util.Date objects to java.time.LocalDate
     * Necessary because javafx.scene.control.DatePicker uses LocalDate
     */
    private static LocalDate toLocalDate(Date date)
    {
        if (date == null)
        {
            return LocalDate.now();
        }

        Instant dateInstant = date.toInstant();
        return dateInstant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /*
     * Converts java.time.LocalDate objects to java.util.Date
     * Necessary because javafx.scene.control.DatePicker uses LocalDate
     */
    private static Date fromLocalDate(LocalDate localDate)
    {
        if (localDate == null)
        {
            return Date.from(Instant.now());
        }

        Instant dateInstant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(dateInstant);
    }

    /* Returns main stage for DetailsView */
    private Stage getStage()
    {
        return (Stage) this.cancelButton.getScene().getWindow();
    }

    /* Initialises ChoiceBoxes for case, client, court and court type */
    private void initChoiceBoxes()
    {
        // Populate ChoiceBoxes
        this.caseChoiceBox.getItems().addAll(this.dm.listCases());
        this.clientChoiceBox.getItems().addAll(this.dm.listClients());
        this.courtChoiceBox.getItems().addAll(this.dm.listCourts());
        this.courtTypeChoiceBox.getItems().addAll(LCourt.CourtTypes.values());

        // Catch ChoiceBox selection change to update other fields
        // Adapted from zhujik's answer at https://stackoverflow.com/a/14523434/7970195
        this.caseChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) ->
        {
            LCase selected = this.caseChoiceBox.getItems().get((Integer) number2);
            this.changeCaseTo(selected);
        });

        this.clientChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) ->
        {
            LClient selected = this.clientChoiceBox.getItems().get((Integer) number2);
            this.changeClientTo(selected);
        });

        this.courtChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) ->
        {
            LCourt selected = this.courtChoiceBox.getItems().get((Integer) number2);
            this.changeCourtTo(selected);
        });
    }

    /* Initialises buttons for new case, client and court creation */
    private void initNewButtons()
    {
        this.newCaseButton.setOnAction(e ->
        {
            // Ask for new case name
            String name = this.askName();

            if (name != null)
            {
                // Generate new case with defaults
                LCase newCase = new LCase(name, this.courtChoiceBox.getValue(), this.clientChoiceBox.getValue(),
                        fromLocalDate(this.dateAssignedDatePicker.getValue()));

                // Add new case to DocumentManager
                this.dm.addCase(newCase);

                // Update fields
                this.caseChoiceBox.getItems().add(newCase);
                this.changeCaseTo(newCase);
            }
        });

        this.newClientButton.setOnAction(e ->
        {
            // Ask for new client name
            String name = this.askName();

            if (name != null)
            {
                // Generate new client with defaults
                LClient newClient = new LClient(name);

                // Add new client to DocumentManager
                this.dm.addClient(newClient);

                // Update fields
                this.clientChoiceBox.getItems().add(newClient);
                this.changeClientTo(newClient);
            }
        });

        this.newCourtButton.setOnAction(e ->
        {
            // Ask for new court name
            String name = this.askName();

            if (name != null)
            {
                // Generate new court with defaults
                LCourt newCourt = new LCourt(name);

                // Add new court to DocumentManager
                this.dm.addCourt(newCourt);

                // Update fields
                this.courtChoiceBox.getItems().add(newCourt);
                this.changeCourtTo(newCourt);
            }
        });
    }

    /*
     *  Creates dialogue asking user for a name
     *  Used for case, client and court creation
     */
    private String askName()
    {
        AskNameController askNameController = new AskNameController();

        // Load view and set controller
        FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("askNameView.fxml"));
        loader.setController(askNameController);

        try
        {
            Parent root = loader.load();

            // Set up stage and scene
            Scene askNameScene = new Scene(root);
            Stage askNameStage = new Stage();

            askNameStage.setScene(askNameScene);
            askNameStage.setTitle("Enter name");

            // Show stage
            askNameStage.showAndWait();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return askNameController.getName();
    }

    /* Changes all fields to match new document */
    private void changeDocumentTo(Document doc)
    {
        this.docNameTextField.setText(doc.getName());

        this.changeCaseTo(doc.getCase());
    }

    /* Changes all fields to match new case */
    private void changeCaseTo(LCase lCase)
    {
        if (lCase != null)
        {
            this.caseChoiceBox.setValue(lCase);
            this.caseNameTextField.setText(lCase.getName());

            this.dateAssignedDatePicker.setValue(toLocalDate(lCase.getDateAssigned()));

            this.changeClientTo(lCase.getClient());
            this.changeCourtTo(lCase.getCourt());
        }
    }

    /* Changes all fields to match new client */
    private void changeClientTo(LClient lClient)
    {
        if (lClient != null)
        {
            this.clientChoiceBox.setValue(lClient);

            this.clientNameTextField.setText(lClient.getName());
            this.clientEmailTextField.setText(lClient.getEmail());
            this.clientPhoneTextField.setText(lClient.getPhone());
        }
    }

    /* Changes all fields to match new court */
    private void changeCourtTo(LCourt lCourt)
    {
        if (lCourt != null)
        {
            this.courtChoiceBox.setValue(lCourt);

            this.courtNameTextField.setText(lCourt.getName());
            this.courtTypeChoiceBox.setValue(lCourt.getType());
        }
    }

    /* Saves changes to the document */
    private void saveChanges()
    {
        LCase lCase = this.caseChoiceBox.getValue();
        LClient lClient = this.clientChoiceBox.getValue();
        LCourt lCourt = this.courtChoiceBox.getValue();

        // Change client details
        if (lClient != null)
        {
            lClient.setName(this.clientNameTextField.getText());

            String email = this.clientEmailTextField.getText();
            String phone = this.clientPhoneTextField.getText();

            if (email != null)
            {
                lClient.setEmail(email);
            }

            if (phone != null)
            {
                lClient.setPhone(phone);
            }
        }

        // Change court details
        if (lCourt != null)
        {
            lCourt.setType(this.courtTypeChoiceBox.getValue());
            lCourt.setName(this.courtNameTextField.getText());
        }

        // Change case details
        if (lCase != null)
        {
            lCase.setName(this.caseNameTextField.getText());
            lCase.setDateAssigned(fromLocalDate(this.dateAssignedDatePicker.getValue()));
            lCase.setClient(lClient);
            lCase.setCourt(lCourt);
        }

        // Save changes to document
        this.doc.setName(this.docNameTextField.getText());
        this.doc.setCase(lCase);

        // Ensure document added to DocumentManager
        if (!this.dm.listDocuments().contains(this.doc))
        {
            this.dm.addAndSerialiseDocument(this.doc);
            this.mainController.refreshDocsDetails();
        }

        // Update details of any documents with the same case, client or court
        for (Document curDoc : this.dm.listDocuments())
        {
            if (lClient != null && curDoc.getClient() == this.clientChoiceBox.getValue())
            {
                curDoc.getCase().setClient(lClient);
            }

            if (lCourt != null && curDoc.getCourt() == this.courtChoiceBox.getValue())
            {
                curDoc.getCase().setCourt(lCourt);
            }

            if (lCase != null && curDoc.getCase() == this.caseChoiceBox.getValue())
            {
                curDoc.setCase(lCase);
            }
        }
    }

    /* Initialises all components of the details view */
    @Override
    public void initialize(URL url, ResourceBundle resources)
    {
        this.initChoiceBoxes();
        this.initNewButtons();

        // Set current document
        this.changeDocumentTo(this.doc);

        // Set confirmation/cancel actions
        this.cancelButton.setOnAction(e -> this.getStage().close());
        this.confirmationButton.setOnAction(e ->
        {
            // On confirmation, the changes must be saved and the document table must be updated before closing
            this.saveChanges();
            this.mainController.refreshDocsDetails();
            this.getStage().close();
        });

        // Adapted from Uluk Biy's answer at https://stackoverflow.com/a/23041348/7970195
        BooleanBinding clientDataInvalid = new BooleanBinding()
        {
            {
                super.bind(DetailsController.this.clientEmailTextField.textProperty(),
                        DetailsController.this.clientPhoneTextField.textProperty());
            }

            @Override
            protected boolean computeValue()
            {
                String email = DetailsController.this.clientEmailTextField.getText();
                String phone = DetailsController.this.clientPhoneTextField.getText();

                // Allow confirmation button if no email/phone is entered and disable if at least one is invalid
                boolean emailInvalid = (email != null) && (!email.isBlank()) && (!LClient.validateEmail(email));
                boolean phoneInvalid = (phone != null) && (!phone.isBlank()) && (!LClient.validatePhone(phone));

                return emailInvalid || phoneInvalid;
            }
        };

        // Bind warning message visibility and confirmation button to invalid data
        this.invalidWarning.visibleProperty().bind(clientDataInvalid);
        this.confirmationButton.disableProperty().bind(clientDataInvalid);

        // Adapted from Johan Kaewberg's answer at https://stackoverflow.com/a/53186959/7970195
        this.dateAssignedDatePicker.setDayCellFactory(d -> new DateCell()
        {
            @Override
            public void updateItem(LocalDate item, boolean empty)
            {
                super.updateItem(item, empty);
                // Disable dates after current time
                this.setDisable(item.isAfter(LocalDate.now()));
            }
        });

        // Binding for Document not having been assigned a case
        BooleanBinding doesNotHaveCase = new BooleanBinding()
        {
            {
                super.bind(DetailsController.this.caseChoiceBox.valueProperty());
            }

            @Override
            protected boolean computeValue()
            {
                return DetailsController.this.caseChoiceBox.getValue() == null;
            }
        };

        // Disable all case-specific fields if no case is selected
        this.clientChoiceBox.disableProperty().bind(doesNotHaveCase);
        this.newClientButton.disableProperty().bind(doesNotHaveCase);
        this.clientNameTextField.disableProperty().bind(doesNotHaveCase);
        this.clientEmailTextField.disableProperty().bind(doesNotHaveCase);
        this.clientPhoneTextField.disableProperty().bind(doesNotHaveCase);

        this.courtChoiceBox.disableProperty().bind(doesNotHaveCase);
        this.newCourtButton.disableProperty().bind(doesNotHaveCase);
        this.courtNameTextField.disableProperty().bind(doesNotHaveCase);
        this.courtTypeChoiceBox.disableProperty().bind(doesNotHaveCase);

        this.dateAssignedDatePicker.disableProperty().bind(doesNotHaveCase);
    }
}
