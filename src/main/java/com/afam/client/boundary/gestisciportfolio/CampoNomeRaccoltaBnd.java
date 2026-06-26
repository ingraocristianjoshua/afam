package com.afam.client.boundary.gestisciportfolio;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Map;

/**
 * CampoNomeRaccoltaBnd – form per la creazione di una nuova raccolta in un portfolio.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class CampoNomeRaccoltaBnd {

    @FXML private Label labelTitolo;
    @FXML private TextField fieldNome;
    @FXML private Button btnAzione;

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> portfolio;
    private Map<String, Object> raccolta; // Se presente, siamo in modalità Rinomina

    public void setPortfolio(Map<String, Object> p) { 
        this.portfolio = p; 
    }

    public void setRaccoltaDaRinominare(Map<String, Object> p, Map<String, Object> r) {
        this.portfolio = p;
        this.raccolta = r;
        labelTitolo.setText("Rinomina raccolta");
        btnAzione.setText("Salva");
        fieldNome.setText((String) r.get("nome"));
    }

    @FXML
    public void onAzione() {
        if (portfolio == null) { MessErrBnd.create("Portfolio non impostato."); return; }
        String nome = fieldNome.getText().trim();
        if (nome.isEmpty()) { MessErrBnd.create("Inserisci un nome per la raccolta."); return; }
        try {
            if (raccolta == null) {
                // Modalità CREA
                rest.post("portfolio/" + portfolio.get("idPortfolio") + "/raccolte",
                        Map.of("nomeRaccolta", nome));
                MessSuccessoBnd.create("Raccolta \"" + nome + "\" creata.");
            } else {
                // Modalità RINOMINA
                rest.patch("portfolio/" + portfolio.get("idPortfolio") + "/raccolte/" + raccolta.get("idRaccolta") + "/nome",
                        Map.of("nomeRaccolta", nome));
                MessSuccessoBnd.create("Raccolta rinominata in \"" + nome + "\".");
            }
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
