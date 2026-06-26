package com.afam.client.boundary.autenticati;

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
 * InserisciEmailBnd – form per la richiesta di recupero password.
 * L'utente inserisce la propria email; il server invia un OTP via email.
 * Dopo l'invio, la schermata naviga a FormOTP in modalità recupero password.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class InserisciEmailBnd {

    @FXML private TextField campoEmail;
    @FXML private Label     labelErrore;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
    }

    public Map<String, Object> getDati() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("email", campoEmail.getText().trim());
        return dati;
    }

    @FXML
    public void onInviaOTP() {
        labelErrore.setVisible(false);
        Map<String, Object> dati = getDati();

        try {
            Map<String, Object> resp = rest.post("auth/recupera-password/richiedi", dati);
            String scadenza = (String) resp.get("scadenza");
            apriFormOTP(scadenza, (String) dati.get("email"));
        } catch (RestClient.RestException e) {
            visualizzaErrore(e.isConnectionError()
                    ? "Server non raggiungibile."
                    : e.getMessage());
        }
    }

    @FXML
    public void onIndietro() {
        apriSchermata("/fxml/autenticati/AccediForm.fxml", "Accedi");
    }

    public void visualizzaErrore(String messaggio) {
        labelErrore.setText(messaggio);
        labelErrore.setVisible(true);
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
