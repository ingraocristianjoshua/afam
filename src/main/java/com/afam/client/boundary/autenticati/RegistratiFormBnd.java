package com.afam.client.boundary.autenticati;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * RegistratiFormBnd – form di registrazione nuovo account.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class RegistratiFormBnd {

    @FXML private TextField     campoNome;
    @FXML private TextField     campoCognome;
    @FXML private DatePicker    campoDataNascita;
    @FXML private TextField     campoEmail;
    @FXML private PasswordField campoPassword;
    @FXML private PasswordField campoConfermaPassword;
    @FXML private TextField     campoTelefono;
    @FXML private Label         labelErrore;
    @FXML private Button        btnRegistrati;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
        campoDataNascita.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate d)   { return d != null ? FMT.format(d) : ""; }
            @Override public LocalDate fromString(String s) {
                try { return (s != null && !s.isBlank()) ? LocalDate.parse(s, FMT) : null; }
                catch (Exception e) { return null; }
            }
        });
        campoDataNascita.setPromptText("gg/mm/aaaa");
    }

    public Map<String, Object> getDati() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("nome",           campoNome.getText().trim());
        dati.put("cognome",        campoCognome.getText().trim());
        dati.put("email",          campoEmail.getText().trim());
        dati.put("password",       campoPassword.getText());
        dati.put("numeroTelefono", campoTelefono.getText().trim());
        LocalDate dn = campoDataNascita.getValue();
        if (dn != null) dati.put("dataNascita", dn.toString()); // ISO yyyy-MM-dd per il server
        return dati;
    }

    @FXML
    public void onRegistrati() {
        nascondErrore();
        if (!campoPassword.getText().equals(campoConfermaPassword.getText())) {
            visualizzaErrore("Le password non coincidono.");
            return;
        }
        Map<String, Object> dati = getDati();
        if (btnRegistrati != null) btnRegistrati.setDisable(true);

        new Thread(() -> {
            try {
                rest.post("auth/registra", dati);
                Platform.runLater(() -> {
                    MessSuccessoBnd.create("Account creato con successo! Ora puoi accedere.");
                    apriSchermata("/fxml/autenticati/AccediForm.fxml", "Accedi");
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> {
                    if (btnRegistrati != null) btnRegistrati.setDisable(false);
                    if (e.getStatusCode() == 409) {
                        visualizzaErrore("Email già in uso. Scegli un indirizzo diverso o accedi.");
                    } else if (e.isConnectionError()) {
                        visualizzaErrore("Server non raggiungibile. Controlla la connessione.");
                    } else {
                        visualizzaErrore(e.getMessage());
                    }
                });
            }
        }, "registra").start();
    }

    @FXML
    public void onIndietro() {
        apriSchermata("/fxml/autenticati/AuthPage.fxml", "Benvenuto");
    }

    public void visualizzaErrore(String messaggio) {
        labelErrore.setText(messaggio);
        labelErrore.setVisible(true);
        labelErrore.setManaged(true);
    }

    private void nascondErrore() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    public void chiudi() {
        Stage stage = (Stage) campoEmail.getScene().getWindow();
        stage.close();
    }

    private void apriSchermata(String path, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Stage stage = new Stage();
            stage.setTitle("AFAM – " + titolo);
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            stage.show();
            chiudi();
        } catch (Exception e) {
            visualizzaErrore("Errore apertura schermata: " + e.getMessage());
        }
    }
}
