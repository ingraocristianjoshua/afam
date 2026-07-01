package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

/**
 * CambiaEmailBnd – flusso a due step per il cambio email con verifica OTP.
 * Step 1: l'utente inserisce la nuova email → server invia OTP alla nuova email.
 * Step 2: l'utente inserisce il codice OTP → server aggiorna l'email.
 */
public class CambiaEmailBnd {

    // ── Campi ──────────────────
    @FXML private VBox   panelEmail;
    @FXML private VBox   panelOTP;
    @FXML private TextField campoNuovaEmail;
    @FXML private TextField campoOTP;
    @FXML private Label  labelIstruzione;
    @FXML private Label  labelErrore;

    private final RestClient rest = RestClient.getInstance();
    private String nuovaEmailInAttesa;

    // ── Metodi ──────────────────
    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
        panelOTP.setVisible(false);
        panelOTP.setManaged(false);
    }

    public Map<String, Object> getDati() {
        return Map.of("nuovaEmail", campoNuovaEmail.getText().trim());
    }

    /** Gestisce l'azione «Invia OTP». */
    @FXML
    public void onInviaOTP() {
        mostraErrore(null);
        nuovaEmailInAttesa = campoNuovaEmail.getText().trim();
        if (nuovaEmailInAttesa.isEmpty()) { mostraErrore("Inserisci la nuova email."); return; }
        try {
            rest.post("account/cambia-email/richiedi", Map.of("nuovaEmail", nuovaEmailInAttesa));
            panelEmail.setVisible(false);
            panelEmail.setManaged(false);
            panelOTP.setVisible(true);
            panelOTP.setManaged(true);
            labelIstruzione.setText("OTP inviato a " + nuovaEmailInAttesa + ". Inserisci il codice ricevuto.");
        } catch (RestClient.RestException e) {
            mostraErrore(e.isConnectionError() ? "Server non raggiungibile." : e.getMessage());
        }
    }

    /** Gestisce l'azione «Conferma OTP». */
    @FXML
    public void onConfermaOTP() {
        mostraErrore(null);
        String otp = campoOTP.getText().trim();
        if (otp.isEmpty()) { mostraErrore("Inserisci il codice OTP."); return; }
        try {
            rest.post("account/cambia-email/conferma", Map.of("otp", otp));
            MessSuccessoBnd.create("Email aggiornata a " + nuovaEmailInAttesa + ".");
            chiudi();
        } catch (RestClient.RestException e) {
            mostraErrore(e.isConnectionError() ? "Server non raggiungibile." : e.getMessage());
        }
    }

    /** Gestisce l'azione «Annulla». */
    @FXML
    public void onAnnulla() { chiudi(); }

    /** Chiude la finestra corrente. */
    public void chiudi() {
        ((Stage) campoNuovaEmail.getScene().getWindow()).close();
    }

    /** Mostra il messaggio di errore indicato. */
    private void mostraErrore(String msg) {
        if (msg == null || msg.isEmpty()) {
            labelErrore.setVisible(false);
            labelErrore.setManaged(false);
        } else {
            labelErrore.setText(msg);
            labelErrore.setVisible(true);
            labelErrore.setManaged(true);
        }
    }
}
