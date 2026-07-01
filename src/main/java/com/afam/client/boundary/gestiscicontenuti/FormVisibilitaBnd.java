package com.afam.client.boundary.gestiscicontenuti;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import com.afam.utils.Constants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Map;

/**
 * FormVisibilitaBnd – form di selezione della visibilità di un contenuto.
 * Come da sequence diagram ImpostaVisibilitaContenuto: mostra la visibilità
 * corrente, l'utente ne seleziona una e conferma; il valore viene validato e
 * inviato al server (PATCH contenuti/{id}/visibilita).
 */
public class FormVisibilitaBnd {

    // ── Campi ──────────────────
    @FXML private Label          labelContenuto;
    @FXML private ComboBox<String> comboVisibilita;
    @FXML private Label          labelErrore;
    @FXML private Button         btnSalva;

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> contenuto;
    private Runnable onSuccesso;

    // ── Metodi ──────────────────
    @FXML
    public void initialize() {
        comboVisibilita.getItems().addAll(Constants.VIS_PRIVATO, Constants.VIS_PUBBLICO);
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    /** Imposta il contenuto da modificare e seleziona la visibilità corrente. */
    public void setContenuto(Map<String, Object> c, Runnable onSuccesso) {
        this.contenuto  = c;
        this.onSuccesso = onSuccesso;
        labelContenuto.setText("Contenuto: " + c.getOrDefault("titolo", ""));
        comboVisibilita.setValue((String) c.getOrDefault("visibilita", Constants.VIS_PRIVATO));
    }

    /** Gestisce l'azione «Salva». */
    @FXML
    public void onSalva() {
        String scelta = comboVisibilita.getValue();
        if (scelta == null || scelta.isBlank()) {
            mostraErrore("Seleziona una visibilità.");
            return;
        }
        Object id = contenuto.get("idContenuto");
        if (id == null) { mostraErrore("ID contenuto non disponibile."); return; }
        nascondiErrore();
        btnSalva.setDisable(true);
        new Thread(() -> {
            try {
                rest.patch("contenuti/" + id + "/visibilita", Map.of("visibilita", scelta));
                Platform.runLater(() -> {
                    MessSuccessoBnd.create("Visibilità aggiornata: " + scelta);
                    if (onSuccesso != null) onSuccesso.run();
                    chiudi();
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> {
                    btnSalva.setDisable(false);
                    mostraErrore("Errore: " + e.getMessage());
                });
            }
        }, "imposta-visibilita-contenuto").start();
    }

    /** Mostra il messaggio di errore indicato. */
    private void mostraErrore(String msg) {
        labelErrore.setText(msg);
        labelErrore.setVisible(true);
        labelErrore.setManaged(true);
    }

    /** Nasconde il messaggio di errore. */
    private void nascondiErrore() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    /** Chiude la finestra corrente. */
    private void chiudi() {
        ((Stage) btnSalva.getScene().getWindow()).close();
    }
}
