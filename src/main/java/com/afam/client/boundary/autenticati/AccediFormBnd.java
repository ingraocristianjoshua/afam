package com.afam.client.boundary.autenticati;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * AccediFormBnd – form di login con email e password.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class AccediFormBnd {

    @FXML private TextField     campoEmail;
    @FXML private PasswordField campoPassword;
    @FXML private Label         labelErrore;
    @FXML private Button        btnAccedi;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    public Map<String, Object> getDati() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("email",    campoEmail.getText().trim());
        dati.put("password", campoPassword.getText());
        return dati;
    }

    public String getEmail() { return campoEmail.getText().trim(); }

    @FXML
    public void onAccedi() {
        nascondErrore();
        Map<String, Object> dati = getDati();
        if (btnAccedi != null) btnAccedi.setDisable(true);

        new Thread(() -> {
            try {
                Map<String, Object> resp = rest.post("auth/accedi", dati);
                String idUtente = (String) resp.get("idUtente");
                rest.setCurrentUserId(idUtente);
                Boolean richiede2FA = (Boolean) resp.get("richiede2FA");
                if (Boolean.TRUE.equals(richiede2FA)) {
                    avvia2FA();
                } else {
                    Platform.runLater(() -> {
                        visualizza("Accesso effettuato. Benvenuto!");
                        apriSchermata("/fxml/homepage/HomePage.fxml", "Home Page");
                    });
                }
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> {
                    if (btnAccedi != null) btnAccedi.setDisable(false);
                    visualizzaErrore(e.isConnectionError()
                            ? "Server non raggiungibile. Controlla la connessione."
                            : e.getMessage());
                });
            }
        }, "accedi").start();
    }

    @FXML
    public void onRecuperaPassword() {
        apriSchermata("/fxml/autenticati/InserisciEmail.fxml", "Inserisci email");
    }

    @FXML
    public void onIndietro() {
        apriSchermata("/fxml/autenticati/AuthPage.fxml", "Benvenuto");
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
        Stage stage = (Stage) campoEmail.getScene().getWindow();
        stage.close();
    }

    private void avvia2FA() {
        try {
            Map<String, Object> risposta = rest.post("auth/invia-2fa", Map.of());
            Platform.runLater(() -> {
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
                    if (btnAccedi != null) btnAccedi.setDisable(false);
                    visualizzaErrore("Impossibile aprire la schermata OTP: " + e.getMessage());
                }
            });
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> {
                if (btnAccedi != null) btnAccedi.setDisable(false);
                visualizzaErrore("Errore invio OTP: " + e.getMessage());
            });
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
            visualizzaErrore("Impossibile aprire la schermata: " + e.getMessage());
        }
    }
}
