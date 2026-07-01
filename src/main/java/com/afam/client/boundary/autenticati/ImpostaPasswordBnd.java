package com.afam.client.boundary.autenticati;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * ImpostaPasswordBnd – form per impostare la nuova password nel flusso di recupero.
 */
public class ImpostaPasswordBnd {

    // ── Campi ──────────────────
    @FXML private PasswordField campoNuovaPassword;
    @FXML private Label         labelErrore;

    private final RestClient rest = RestClient.getInstance();

    private String email;
    private String otp;
    private String scadenza;

    // ── Metodi ──────────────────
    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    /** Configura. */
    public void configura(String email, String otp, String scadenza) {
        this.email    = email;
        this.otp      = otp;
        this.scadenza = scadenza;
    }

    public Map<String, Object> getDati() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("email",         email);
        dati.put("otp",           otp);
        dati.put("scadenza",      scadenza);
        dati.put("nuovaPassword", campoNuovaPassword.getText());
        return dati;
    }

    /** Gestisce l'azione «Conferma». */
    @FXML
    public void onConferma() {
        nascondErrore();
        String nuova = campoNuovaPassword.getText();
        if (nuova.isBlank()) { visualizzaErrore("Inserisci una password valida."); return; }

        new Thread(() -> {
            try {
                rest.post("auth/recupera-password/reimposta", getDati());
                Platform.runLater(() -> {
                    MessSuccessoBnd.create("Password reimpostata con successo.");
                    apriSchermata("/fxml/autenticati/AccediForm.fxml", "Accedi");
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> visualizzaErrore(e.isConnectionError()
                        ? "Server non raggiungibile."
                        : e.getMessage()));
            }
        }, "reimposta-password").start();
    }

    /** Gestisce l'azione «Indietro». */
    @FXML
    public void onIndietro() {
        apriSchermata("/fxml/autenticati/AuthPage.fxml", "Benvenuto");
    }

    /** Mostra il messaggio di errore indicato. */
    public void visualizzaErrore(String messaggio) {
        labelErrore.setText(messaggio);
        labelErrore.setVisible(true);
        labelErrore.setManaged(true);
    }

    /** Nasconde il messaggio di errore. */
    private void nascondErrore() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    /** Chiude la finestra corrente. */
    public void chiudi() {
        Stage stage = (Stage) campoNuovaPassword.getScene().getWindow();
        stage.close();
    }

    /** Apre la schermata FXML indicata. */
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
