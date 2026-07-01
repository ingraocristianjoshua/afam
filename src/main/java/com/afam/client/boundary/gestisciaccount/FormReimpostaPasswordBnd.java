package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * FormReimpostaPasswordBnd – form cambio password per utente autenticato.
 */
public class FormReimpostaPasswordBnd {

    // ── Campi ──────────────────
    @FXML private PasswordField campoVecchia;
    @FXML private PasswordField campoNuova;
    @FXML private Label         labelErrore;

    private final RestClient rest = RestClient.getInstance();

    // ── Metodi ──────────────────
    @FXML public void initialize() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    public Map<String, Object> getDati() {
        Map<String, Object> d = new HashMap<>();
        d.put("vecchiaPassword", campoVecchia.getText());
        d.put("nuovaPassword",   campoNuova.getText());
        return d;
    }

    /** Gestisce l'azione «Conferma». */
    @FXML
    public void onConferma() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
        if (campoNuova.getText().isBlank()) { mostraErrore("Inserisci la nuova password."); return; }

        Map<String, Object> dati = getDati();
        new Thread(() -> {
            try {
                rest.post("account/reimposta-password", dati);
                Platform.runLater(() -> {
                    MessSuccessoBnd.create("Password aggiornata con successo.");
                    chiudi();
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> mostraErrore(e.getMessage()));
            }
        }, "reimposta-password-auth").start();
    }

    @FXML public void onAnnulla() { chiudi(); }

    /** Mostra il messaggio di errore indicato. */
    public void mostraErrore(String msg) {
        labelErrore.setText(msg);
        labelErrore.setVisible(true);
        labelErrore.setManaged(true);
    }

    /** Chiude la finestra corrente. */
    public void chiudi() { ((Stage) campoVecchia.getScene().getWindow()).close(); }
}
