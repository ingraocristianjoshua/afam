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
 * RegistratiFormBnd – form di registrazione nuovo account.
 *
 * Flusso (sequence): getDati → POST /auth/registra → visualizza esito.
 * Se il server risponde 409 (email già in uso): visualizza messaggio specifico
 * e lascia il form aperto (loop sui dati non validi come da spec).
 * Se il server risponde 400 (dati non validi): mostra l'errore e lascia aperto.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class RegistratiFormBnd {

    @FXML private TextField     campoNome;
    @FXML private TextField     campoCognome;
    @FXML private TextField     campoEmail;
    @FXML private PasswordField campoPassword;
    @FXML private PasswordField campoConfermaPassword;
    @FXML private TextField     campoTelefono;
    @FXML private Label         labelErrore;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
    }

    // ── Raccolta input ────────────────────────────────────────────────────────

    public Map<String, Object> getDati() {
        Map<String, Object> dati = new HashMap<>();
        dati.put("nome",            campoNome.getText().trim());
        dati.put("cognome",         campoCognome.getText().trim());
        dati.put("email",           campoEmail.getText().trim());
        dati.put("password",        campoPassword.getText());
        dati.put("numeroTelefono",  campoTelefono.getText().trim());
        return dati;
    }

    public String getEmail() {
        return campoEmail.getText().trim();
    }

    // ── Handler pulsante Registrati ───────────────────────────────────────────

    @FXML
    public void onRegistrati() {
        labelErrore.setVisible(false);

        // controllo locale: conferma password (non è logica di dominio)
        if (!campoPassword.getText().equals(campoConfermaPassword.getText())) {
            visualizzaErrore("Le password non coincidono.");
            return;
        }

        Map<String, Object> dati = getDati();

        try {
            Map<String, Object> resp = rest.post("auth/registra", dati);
            // successo → torna al login
            visualizza("Account creato con successo! Ora puoi accedere.");
            apriSchermata("/fxml/autenticati/AccediForm.fxml", "Accedi");

        } catch (RestClient.RestException e) {
            if (e.getStatusCode() == 409) {
                // email già in uso: loop aperto come da spec
                visualizzaErrore("Email già in uso. Scegli un indirizzo diverso o accedi.");
            } else if (e.isConnectionError()) {
                visualizzaErrore("Server non raggiungibile. Controlla la connessione.");
            } else {
                // dati non validi: loop aperto come da spec
                visualizzaErrore(e.getMessage());
            }
        }
    }

    @FXML
    public void onIndietro() {
        apriSchermata("/fxml/autenticati/AuthPage.fxml", "Benvenuto");
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
        Stage stage = (Stage) campoEmail.getScene().getWindow();
        stage.close();
    }

    // ── Helper ───────────────────────────────────────────────────────────────

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
