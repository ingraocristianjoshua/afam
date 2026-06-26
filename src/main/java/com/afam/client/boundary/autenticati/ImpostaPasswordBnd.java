package com.afam.client.boundary.autenticati;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * ReimpostaPasswordBnd – form per impostare la nuova password.
 * Usato nel flusso unauthenticated di recupero password, dopo la verifica OTP.
 * Riceve email, otp e scadenza dal chiamante via configura().
 * @author Cristian Joshua Ingrao (0780672)
 */
public class ImpostaPasswordBnd {

    @FXML private PasswordField campoNuovaPassword;
    @FXML private Label         labelErrore;

    private final RestClient rest = RestClient.getInstance();

    private String email;
    private String otp;
    private String scadenza;

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
    }

    /** Chiamato da FormOTPBnd dopo la verifica OTP. */
    public void configura(String email, String otp, String scadenza) {
        this.email    = email;
        this.otp      = otp;
        this.scadenza = scadenza;
    }

    public Map<String, Object> getDati() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("email",          email);
        dati.put("otp",            otp);
        dati.put("scadenza",       scadenza);
        dati.put("nuovaPassword",  campoNuovaPassword.getText());
        return dati;
    }

    @FXML
    public void onConferma() {
        labelErrore.setVisible(false);

        String nuova   = campoNuovaPassword.getText();
        if (nuova.isBlank()) {
            visualizzaErrore("Inserisci una password valida.");
            return;
        }

        try {
            rest.post("auth/recupera-password/reimposta", getDati());
            MessSuccessoBnd.create("Password reimpostata con successo.");
            apriSchermata("/fxml/autenticati/AccediForm.fxml", "Accedi");
        } catch (RestClient.RestException e) {
            visualizzaErrore(e.isConnectionError()
                    ? "Server non raggiungibile."
                    : e.getMessage());
        }
    }

    @FXML
    public void onIndietro() {
        apriSchermata("/fxml/autenticati/AuthPage.fxml", "Benvenuto");
    }

    public void visualizzaErrore(String messaggio) {
        labelErrore.setText(messaggio);
        labelErrore.setVisible(true);
    }

    public void chiudi() {
        Stage stage = (Stage) campoNuovaPassword.getScene().getWindow();
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
