package com.afam.client.boundary.autenticati;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * FormOTPBnd – form per l'inserimento del codice OTP.
 * Usato sia per la verifica 2FA che per il recupero password.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class FormOTPBnd {

    @FXML private TextField campoOTP;
    @FXML private Label     labelIstruzione;
    @FXML private Label     labelErrore;
    @FXML private Button    btnConferma;

    private final RestClient rest = RestClient.getInstance();

    private String  scadenzaOTP;
    private boolean flusso2FA = true;
    private String  emailUtente;

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
        labelIstruzione.setText("Codice OTP visibile nel terminale del server.");
    }

    public void configura(String scadenzaOTP, boolean flusso2FA, String emailUtente) {
        this.scadenzaOTP  = scadenzaOTP;
        this.flusso2FA    = flusso2FA;
        this.emailUtente  = emailUtente;
        labelIstruzione.setText(flusso2FA
                ? "Codice OTP visibile nel terminale del server (SMS simulato)."
                : "Codice OTP visibile nel terminale del server (email simulata).");
    }

    public Map<String, Object> getDati() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("otp",      campoOTP.getText().trim());
        dati.put("scadenza", scadenzaOTP);
        if (emailUtente != null) dati.put("email", emailUtente);
        return dati;
    }

    @FXML
    public void onVerifica() {
        nascondErrore();
        Map<String, Object> dati = getDati();
        String endpoint = flusso2FA ? "auth/verifica-2fa" : "auth/recupera-password/verifica-otp";
        if (btnConferma != null) btnConferma.setDisable(true);

        new Thread(() -> {
            try {
                rest.post(endpoint, dati);
                if (flusso2FA) {
                    Platform.runLater(() -> {
                        visualizza("Verifica completata.");
                        apriSchermata("/fxml/homepage/HomePage.fxml", "Home");
                    });
                } else {
                    Platform.runLater(() -> {
                        visualizza("Verifica completata.");
                        apriReimpostaPassword(dati);
                    });
                }
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> {
                    if (btnConferma != null) btnConferma.setDisable(false);
                    visualizzaErrore(e.isConnectionError()
                            ? "Server non raggiungibile."
                            : e.getMessage());
                });
            }
        }, "verifica-otp").start();
    }

    @FXML
    public void onInviaDiNuovo() {
        nascondErrore();
        String endpoint = flusso2FA ? "auth/invia-2fa" : "auth/recupera-password/richiedi";
        Map<String, Object> body = flusso2FA
                ? Map.of()
                : Map.of("email", emailUtente != null ? emailUtente : "");

        new Thread(() -> {
            try {
                Map<String, Object> resp = rest.post(endpoint, body);
                scadenzaOTP = (String) resp.get("scadenza");
                Platform.runLater(() -> visualizza("Nuovo codice inviato."));
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> visualizzaErrore("Errore invio codice: " + e.getMessage()));
            }
        }, "reinvia-otp").start();
    }

    public void visualizza(String messaggio) { MessSuccessoBnd.create(messaggio); }

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
        Stage stage = (Stage) campoOTP.getScene().getWindow();
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

    private void apriReimpostaPassword(Map<String, Object> datiOTP) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/autenticati/ImpostaPassword.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AFAM – Reimposta password");
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            ImpostaPasswordBnd ctrl = loader.getController();
            ctrl.configura(
                    (String) datiOTP.get("email"),
                    (String) datiOTP.get("otp"),
                    (String) datiOTP.get("scadenza"));
            stage.show();
            chiudi();
        } catch (Exception e) {
            visualizzaErrore("Errore apertura schermata: " + e.getMessage());
        }
    }
}
