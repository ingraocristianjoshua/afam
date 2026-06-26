package com.afam.client.boundary.autenticati;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * AccediFormBnd – form di login con email e password.
 *
 * Pattern: la boundary raccoglie i dati dal form tramite getDati()
 * e li invia al server via RestClient. Non valida nulla localmente:
 * tutta la logica è nel server (AuthCtrl → DBMSBnd).
 * @author Cristian Joshua Ingrao (0780672)
 */
public class AccediFormBnd {

    @FXML private TextField     campoEmail;
    @FXML private PasswordField campoPassword;
    @FXML private Label         labelErrore;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
    }

    // ── Raccolta input (naming spec: getDati) ─────────────────────────────────

    public Map<String, Object> getDati() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("email",    campoEmail.getText().trim());
        dati.put("password", campoPassword.getText());
        return dati;
    }

    public String getEmail() {
        return campoEmail.getText().trim();
    }

    // ── Handler pulsante Accedi ───────────────────────────────────────────────

    @FXML
    public void onAccedi() {
        labelErrore.setVisible(false);
        Map<String, Object> dati = getDati();

        try {
            Map<String, Object> resp = rest.post("auth/accedi", dati);
            String idUtente = (String) resp.get("idUtente");
            rest.setCurrentUserId(idUtente);

            Boolean richiede2FA = (Boolean) resp.get("richiede2FA");
            if (Boolean.TRUE.equals(richiede2FA)) {
                avvia2FA(idUtente);
            } else {
                visualizza("Accesso effettuato. Benvenuto!");
                apriAreaPersonale();
            }
        } catch (RestClient.RestException e) {
            visualizzaErrore(e.isConnectionError()
                    ? "Server non raggiungibile. Controlla la connessione."
                    : e.getMessage());
        }
    }

    /** Apre il form per il recupero password. */
    @FXML
    public void onRecuperaPassword() {
        apriSchermata("/fxml/autenticati/InserisciEmail.fxml", "Inserisci email");
    }

    /** Torna alla schermata di benvenuto. */
    @FXML
    public void onIndietro() {
        apriSchermata("/fxml/autenticati/AuthPage.fxml", "Benvenuto");
    }

    // ── Visualizzazione risultati (naming spec: visualizza) ───────────────────

    public void visualizza(String messaggio) {
        MessSuccessoBnd.create(messaggio);
    }

    public void visualizzaErrore(String messaggio) {
        labelErrore.setText(messaggio);
        labelErrore.setVisible(true);
    }

    public void chiudi() {
        Stage stage = (Stage) campoEmail.getScene().getWindow();
        stage.close();
    }

    // ── Helper navigazione ────────────────────────────────────────────────────

    private void avvia2FA(String idUtente) {
        Map<String, Object> risposta;
        try {
            risposta = rest.post("auth/invia-2fa", Map.of());
        } catch (RestClient.RestException e) {
            visualizzaErrore("Errore invio OTP: " + e.getMessage());
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/autenticati/FormOTP.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AFAM – Verifica 2FA");
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            FormOTPBnd ctrl = loader.getController();
            ctrl.configura((String) risposta.get("scadenza"), true, null);
            stage.show();
            chiudi();
        } catch (Exception e) {
            visualizzaErrore("Impossibile aprire la schermata OTP: " + e.getMessage());
        }
    }

    private void apriAreaPersonale() {
        apriSchermata("/fxml/homepage/HomePage.fxml", "Home Page");
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
            visualizzaErrore("Impossibile aprire la schermata: " + e.getMessage());
        }
    }
}
