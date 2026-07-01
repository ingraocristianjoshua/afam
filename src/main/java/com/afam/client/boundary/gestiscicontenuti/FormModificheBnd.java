package com.afam.client.boundary.gestiscicontenuti;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import com.afam.utils.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Map;

/**
 * FormModificheBnd – form per la modifica dei metadati di un contenuto esistente.
 * La visibilità non si modifica qui: si gestisce dal comando dedicato in Gestisci
 * Contenuti; qui il valore corrente viene solo conservato invariato.
 */
public class FormModificheBnd {

    // ── Campi ──────────────────
    @FXML private TextField    fieldTitolo;

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> contenuto;

    // ── Metodi ──────────────────
    /** Imposta contenuto. */
    public void setContenuto(Map<String, Object> c) {
        this.contenuto = c;
        fieldTitolo.setText((String) c.getOrDefault("titolo", ""));
    }

    /** Gestisce l'azione «Salva». */
    @FXML
    public void onSalva() {
        String nuovoTitolo = fieldTitolo.getText().trim();
        if (nuovoTitolo.isEmpty()) { MessErrBnd.create("Il titolo non può essere vuoto."); return; }
        try {
            // visibilità invariata: si conserva quella corrente del contenuto
            String visibilita = (String) contenuto.getOrDefault("visibilita", Constants.VIS_PRIVATO);
            rest.put("contenuti/" + contenuto.get("idContenuto"),
                    Map.of("titolo",     nuovoTitolo,
                           "visibilita", visibilita));
            MessSuccessoBnd.create("Contenuto aggiornato.");
            chiudi();
        } catch (RestClient.RestException e) {
            MessErrBnd.create(e.getMessage());
        }
    }

    /** Gestisce l'azione «Annulla». */
    @FXML
    public void onAnnulla() { chiudi(); }

    /** Chiude la finestra corrente. */
    private void chiudi() {
        Stage stage = (Stage) fieldTitolo.getScene().getWindow();
        stage.close();
    }
}
