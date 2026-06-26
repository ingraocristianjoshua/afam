package com.afam.client.boundary.gestiscicontenuti;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import com.afam.utils.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Map;

/**
 * FormModificheBnd – form per la modifica dei metadati di un contenuto esistente.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class FormModificheBnd {

    @FXML private TextField    fieldTitolo;
    @FXML private ComboBox<String> comboVisibilita;

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> contenuto;

    @FXML
    public void initialize() {
        comboVisibilita.getItems().addAll(
                Constants.VIS_PRIVATO, Constants.VIS_PUBBLICO);
    }

    public void setContenuto(Map<String, Object> c) {
        this.contenuto = c;
        fieldTitolo.setText((String) c.getOrDefault("titolo", ""));
        comboVisibilita.setValue((String) c.getOrDefault("visibilita", Constants.VIS_PRIVATO));
    }

    @FXML
    public void onSalva() {
        String nuovoTitolo = fieldTitolo.getText().trim();
        if (nuovoTitolo.isEmpty()) { MessErrBnd.create("Il titolo non può essere vuoto."); return; }
        try {
            rest.put("contenuti/" + contenuto.get("idContenuto"),
                    Map.of("titolo",     nuovoTitolo,
                           "visibilita", comboVisibilita.getValue()));
            MessSuccessoBnd.create("Contenuto aggiornato.");
            chiudi();
        } catch (RestClient.RestException e) {
            MessErrBnd.create(e.getMessage());
        }
    }

    @FXML
    public void onAnnulla() { chiudi(); }

    private void chiudi() {
        Stage stage = (Stage) fieldTitolo.getScene().getWindow();
        stage.close();
    }
}
