package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Map;

/**
 * ValidaEmailBnd – invia OTP all'email e verifica il codice.
 * Riutilizza FormOTPBnd internamente (apre la schermata OTP con il contesto email).
 * @author Cristian Joshua Ingrao (0780672)
 */
public class ValidaEmailBnd {

    @FXML private Label     labelInfo;
    @FXML private TextField campoOTP;
    @FXML private Label     labelErrore;

    private final RestClient rest = RestClient.getInstance();
    private String scadenzaOTP;

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        richiediOTP();
    }

    private void richiediOTP() {
        try {
            Map<String, Object> resp = rest.post("account/valida-email/richiedi", Map.of());
            if (Boolean.TRUE.equals(resp.get("giaValidata"))) {
                labelInfo.setText("La tua email è già validata.");
                return;
            }
            scadenzaOTP = (String) resp.get("scadenza");
            labelInfo.setText("Codice OTP generato. Consultare il terminale del server per il codice.");
        } catch (RestClient.RestException e) {
            mostraErrore("Errore invio OTP: " + e.getMessage());
        }
    }

    public Map<String, Object> getDati() {
        return Map.of("otp", campoOTP.getText().trim());
    }

    @FXML
    public void onVerifica() {
        labelErrore.setVisible(false);
        try {
            rest.post("account/valida-email/conferma", getDati());
            MessSuccessoBnd.create("Email validata con successo.");
            chiudi();
        } catch (RestClient.RestException e) {
            mostraErrore(e.getMessage()); // loop: rimane aperto
        }
    }

    @FXML
    public void onInviaDiNuovo() {
        campoOTP.clear();
        richiediOTP();
    }

    @FXML public void onAnnulla() { chiudi(); }

    public void mostraErrore(String msg) { labelErrore.setText(msg); labelErrore.setVisible(true); }
    public void chiudi() { ((Stage) campoOTP.getScene().getWindow()).close(); }
}
