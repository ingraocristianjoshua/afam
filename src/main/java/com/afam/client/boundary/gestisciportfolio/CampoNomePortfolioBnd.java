package com.afam.client.boundary.gestisciportfolio;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Map;

/**
 * CampoNomePortfolioBnd – form per la creazione di un nuovo portfolio.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class CampoNomePortfolioBnd {

    @FXML private TextField fieldNome;

    private final RestClient rest = RestClient.getInstance();
    private String idCreato;

    /** Id del portfolio appena creato (null se non creato). */
    public String getIdCreato() { return idCreato; }

    @FXML
    @SuppressWarnings("unchecked")
    public void onCrea() {
        String nome = fieldNome.getText().trim();
        if (nome.isEmpty()) { MessErrBnd.create("Inserisci un nome per il portfolio."); return; }
        try {
            Map<String, Object> resp = rest.post("portfolio", Map.of("nomePortfolio", nome));
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            idCreato = data != null ? (String) data.get("idPortfolio") : null;
            MessSuccessoBnd.create("Portfolio \"" + nome + "\" creato.");
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
