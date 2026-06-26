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
 * @author Cristian Joshua Ingrao (0780672)
 */
public class InserisciEmailBnd {

    @FXML private TextField campoEmail;
    @FXML private Label     labelErrore;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

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

    @FXML
    public void onIndietro() {
        apriSchermata("/fxml/autenticati/AccediForm.fxml", "Accedi");
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
