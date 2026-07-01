package com.afam.client.boundary.gestisciportfolio;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * InsiemeContenutiBnd – boundary di selezione di un contenuto da aggiungere al
 * portfolio (come da sequence diagram AggiungiContenuto). Mostra l'elenco dei
 * contenuti disponibili; alla conferma restituisce il contenuto scelto.
 */
public class InsiemeContenutiBnd {

    // ── Campi ──────────────────
    @FXML private ComboBox<Map<String, Object>> comboContenuto;
    @FXML private Button btnConferma;

    private Consumer<Map<String, Object>> onScelto;

    // ── Metodi ──────────────────
    @FXML
    public void initialize() {
        comboContenuto.setCellFactory(lv -> cella());
        comboContenuto.setButtonCell(cella());
    }

    /** Imposta i contenuti selezionabili e la callback da invocare alla scelta. */
    public void setContenuti(List<Map<String, Object>> contenuti, Consumer<Map<String, Object>> onScelto) {
        this.onScelto = onScelto;
        comboContenuto.getItems().setAll(contenuti);
        if (!contenuti.isEmpty()) comboContenuto.getSelectionModel().selectFirst();
    }

    /** Gestisce l'azione «Conferma». */
    @FXML
    public void onConferma() {
        Map<String, Object> sel = comboContenuto.getValue();
        if (sel == null) return;
        if (onScelto != null) onScelto.accept(sel);
        chiudi();
    }

    private ListCell<Map<String, Object>> cella() {
        return new ListCell<>() {
            @Override protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (String) item.getOrDefault("titolo", ""));
            }
        };
    }

    /** Chiude la finestra corrente. */
    private void chiudi() {
        ((Stage) comboContenuto.getScene().getWindow()).close();
    }
}
