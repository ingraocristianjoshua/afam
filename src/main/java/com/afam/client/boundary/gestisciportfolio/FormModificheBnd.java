package com.afam.client.boundary.gestisciportfolio;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Map;

/**
 * FormModificheBnd – form per rinominare una raccolta esistente.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class FormModificheBnd {

    @FXML private TextField fieldNome;

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> portfolio;
    private Map<String, Object> raccolta;

    public void setContesto(Map<String, Object> portfolio, Map<String, Object> raccolta) {
        this.portfolio = portfolio;
        this.raccolta  = raccolta;
        fieldNome.setText((String) raccolta.get("nome"));
    }

    @FXML
    public void onRinomina() {
        String nome = fieldNome.getText().trim();
        if (nome.isEmpty()) { MessErrBnd.create("Inserisci il nuovo nome."); return; }
        try {
            rest.patch(
                    "portfolio/" + portfolio.get("idPortfolio")
                    + "/raccolte/" + raccolta.get("idRaccolta") + "/nome",
                    Map.of("nomeRaccolta", nome));
            MessSuccessoBnd.create("Raccolta rinominata in \"" + nome + "\".");
            chiudi();
        } catch (RestClient.RestException e) {
            MessErrBnd.create(e.getMessage());
        }
    }

    @FXML
    public void onAnnulla() { chiudi(); }

    private void chiudi() {
        Stage stage = (Stage) fieldNome.getScene().getWindow();
        stage.close();
    }
}
