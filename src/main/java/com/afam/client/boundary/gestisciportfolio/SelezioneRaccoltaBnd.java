package com.afam.client.boundary.gestisciportfolio;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * SelezioneRaccoltaBnd – boundary di selezione della raccolta a cui aggiungere
 * un contenuto (come da sequence diagram AggiungiAllaRaccolta). Mostra l'elenco
 * delle raccolte del portfolio; alla conferma restituisce la raccolta scelta.
 */
public class SelezioneRaccoltaBnd {

    // ── Campi ──────────────────
    @FXML private Label labelContenuto;
    @FXML private ComboBox<Map<String, Object>> comboRaccolta;
    @FXML private Button btnConferma;

    private Consumer<Map<String, Object>> onScelto;

    // ── Metodi ──────────────────
    @FXML
    public void initialize() {
        comboRaccolta.setCellFactory(lv -> cella());
        comboRaccolta.setButtonCell(cella());
    }

    /** Imposta le raccolte selezionabili, il contenuto di partenza e la callback. */
    public void setRaccolte(String titoloContenuto, List<Map<String, Object>> raccolte,
                            Consumer<Map<String, Object>> onScelto) {
        this.onScelto = onScelto;
        labelContenuto.setText("Contenuto: " + titoloContenuto);
        comboRaccolta.getItems().setAll(raccolte);
        if (!raccolte.isEmpty()) comboRaccolta.getSelectionModel().selectFirst();
    }

    /** Gestisce l'azione «Conferma». */
    @FXML
    public void onConferma() {
        Map<String, Object> sel = comboRaccolta.getValue();
        if (sel == null) return;
        if (onScelto != null) onScelto.accept(sel);
        chiudi();
    }

    private ListCell<Map<String, Object>> cella() {
        return new ListCell<>() {
            @Override protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (String) item.getOrDefault("nome", ""));
            }
        };
    }

    /** Chiude la finestra corrente. */
    private void chiudi() {
        ((Stage) comboRaccolta.getScene().getWindow()).close();
    }
}
