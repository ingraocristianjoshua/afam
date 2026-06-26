package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/** ReimpostaPasswordBnd – form cambio password per utente autenticato.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class FormReimpostaPasswordBnd {

    @FXML private PasswordField campoVecchia;
    @FXML private PasswordField campoNuova;
    @FXML private PasswordField campoConferma;
    @FXML private Label         labelErrore;

    private final RestClient rest = RestClient.getInstance();

    @FXML public void initialize() { labelErrore.setVisible(false); }

    public Map<String, Object> getDati() {
        Map<String, Object> d = new HashMap<>();
        d.put("vecchiaPassword", campoVecchia.getText());
        d.put("nuovaPassword",   campoNuova.getText());
        return d;
    }

    @FXML
    public void onConferma() {
        labelErrore.setVisible(false);
        if (!campoNuova.getText().equals(campoConferma.getText())) {
            mostraErrore("Le nuove password non coincidono.");
            return;
        }
        try {
            rest.post("account/reimposta-password", getDati());
            MessSuccessoBnd.create("Password aggiornata con successo.");
            chiudi();
        } catch (RestClient.RestException e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML public void onAnnulla() { chiudi(); }

    public void mostraErrore(String msg) { labelErrore.setText(msg); labelErrore.setVisible(true); }
    public void chiudi() { ((Stage) campoVecchia.getScene().getWindow()).close(); }
}
