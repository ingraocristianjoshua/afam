package com.afam.client.boundary.autenticati;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * FormOTPBnd – form per l'inserimento del codice OTP.
 * Usato sia per la verifica 2FA che per il recupero password.
 *
 * Il campo {@code scadenza} viene passato come parametro di inizializzazione
 * dal flusso che apre questa schermata; viene incluso nella richiesta REST
 * per permettere la verifica lato server.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class FormOTPBnd {

    @FXML private TextField campoOTP;
    @FXML private Label     labelIstruzione;
    @FXML private Label     labelErrore;

    private final RestClient rest = RestClient.getInstance();

    /** Scadenza OTP passata dal flusso chiamante (ISO-8601 string). */
    private String  scadenzaOTP;
    /** Indica se siamo nel flusso 2FA (true) o recupero password (false). */
    private boolean flusso2FA = true;
    /** Email utente, usata solo nel flusso recupero password. */
    private String  emailUtente;

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        labelIstruzione.setText("Codice OTP visibile nel terminale del server.");
    }

    /** Chiamato dal flusso chiamante per configurare la schermata. */
    public void configura(String scadenzaOTP, boolean flusso2FA, String emailUtente) {
        this.scadenzaOTP  = scadenzaOTP;
        this.flusso2FA    = flusso2FA;
        this.emailUtente  = emailUtente;
        labelIstruzione.setText(flusso2FA
                ? "Codice OTP visibile nel terminale del server (SMS simulato)."
                : "Codice OTP visibile nel terminale del server (email simulata).");
    }

    // ── Raccolta input ────────────────────────────────────────────────────────

    public Map<String, Object> getDati() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("otp",      campoOTP.getText().trim());
        dati.put("scadenza", scadenzaOTP);
        if (emailUtente != null) dati.put("email", emailUtente);
        return dati;
    }

    // ── Handler ───────────────────────────────────────────────────────────────

    @FXML
    public void onVerifica() {
        labelErrore.setVisible(false);
        Map<String, Object> dati = getDati();
        String endpoint = flusso2FA ? "auth/verifica-2fa" : "auth/recupera-password/verifica-otp";

        try {
            rest.post(endpoint, dati);
            visualizza("Verifica completata.");
            if (flusso2FA) {
                apriSchermata("/fxml/homepage/HomePage.fxml", "Home");
            } else {
                apriReimpostaPassword(dati);
            }
        } catch (RestClient.RestException e) {
            // loop: il form rimane aperto come da sequence diagram
            visualizzaErrore(e.isConnectionError()
                    ? "Server non raggiungibile."
                    : e.getMessage());
        }
    }

    @FXML
    public void onInviaDiNuovo() {
        try {
            String endpoint = flusso2FA ? "auth/invia-2fa" : "auth/recupera-password/richiedi";
            Map<String, Object> body = flusso2FA
                    ? Map.of()
                    : Map.of("email", emailUtente != null ? emailUtente : "");
            Map<String, Object> resp = rest.post(endpoint, body);
            scadenzaOTP = (String) resp.get("scadenza");
            visualizza("Nuovo codice inviato.");
        } catch (RestClient.RestException e) {
            visualizzaErrore("Errore invio codice: " + e.getMessage());
        }
    }

    // ── Visualizzazione ───────────────────────────────────────────────────────

    public void visualizza(String messaggio) {
        MessSuccessoBnd.create(messaggio);
    }

    public void visualizzaErrore(String messaggio) {
        labelErrore.setText(messaggio);
        labelErrore.setVisible(true);
    }

    public void chiudi() {
        Stage stage = (Stage) campoOTP.getScene().getWindow();
        stage.close();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

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
