package com.afam.client.boundary.autenticati;

import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Map;

/**
 * InserisciEmailBnd – form per la richiesta di recupero password.
 */
public class InserisciEmailBnd {

    // ── Campi ──────────────────
    @FXML private TextField campoEmail;
    @FXML private Label     labelErrore;

    private final RestClient rest = RestClient.getInstance();

    // ── Metodi ──────────────────
    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    /** Gestisce l'azione «Invia OTP». */
    @FXML
    public void onInviaOTP() {
        nascondErrore();
        String email = campoEmail.getText().trim();
        if (email.isBlank()) { visualizzaErrore("Inserisci la tua email."); return; }

        new Thread(() -> {
            try {
                Map<String, Object> resp = rest.post("auth/recupera-password/richiedi",
                        Map.of("email", email));
                String scadenza = (String) resp.get("scadenza");
                Platform.runLater(() -> apriFormOTP(scadenza, email));
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> visualizzaErrore(e.isConnectionError()
                        ? "Server non raggiungibile."
                        : e.getMessage()));
            }
        }, "richiedi-otp").start();
    }

    /** Gestisce l'azione «Indietro». */
    @FXML
    public void onIndietro() {
        apriSchermata("/fxml/autenticati/AccediForm.fxml", "Accedi");
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
        Stage stage = (Stage) campoEmail.getScene().getWindow();
        stage.close();
    }

    /** Apre il form di inserimento dell'OTP. */
    private void apriFormOTP(String scadenza, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/autenticati/FormOTP.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AFAM – Verifica OTP");
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            FormOTPBnd ctrl = loader.getController();
            ctrl.configura(scadenza, false, email);
            stage.show();
            chiudi();
        } catch (Exception e) {
            visualizzaErrore("Errore apertura form OTP: " + e.getMessage());
        }
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
