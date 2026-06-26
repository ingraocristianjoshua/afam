package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/** FormSelettore2FABnd – abilita o disabilita l'autenticazione a due fattori.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class FormSelettore2FABnd {

    @FXML private CheckBox  checkAbilita;
    @FXML private TextField campoEmail;
    @FXML private TextField campoNumero;
    @FXML private Label     labelStato;
    @FXML private Label     labelErrore;

    private final RestClient rest = RestClient.getInstance();

    @FXML public void initialize() {
        labelErrore.setVisible(false);
        try {
            Map<String, Object> resp = rest.get("account/2fa");
            boolean stato = Boolean.TRUE.equals(resp.get("stato2FA"));
            checkAbilita.setSelected(stato);
            labelStato.setText("2FA attualmente: " + (stato ? "ATTIVO" : "NON ATTIVO"));
        } catch (RestClient.RestException e) {
            mostraErrore("Impossibile caricare lo stato 2FA: " + e.getMessage());
        }
    }

    public Map<String, Object> getDati() {
        Map<String, Object> d = new HashMap<>();
        d.put("email",   campoEmail.getText().trim());
        d.put("numero",  campoNumero.getText().trim());
        d.put("abilita", checkAbilita.isSelected());
        return d;
    }

    @FXML
    public void onSalva() {
        labelErrore.setVisible(false);
        try {
            rest.post("account/2fa/configura", getDati());
            String msg = checkAbilita.isSelected() ? "2FA abilitato." : "2FA disabilitato.";
            MessSuccessoBnd.create(msg);
            chiudi();
        } catch (RestClient.RestException e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML public void onAnnulla() { chiudi(); }

    public void mostraErrore(String msg) { labelErrore.setText(msg); labelErrore.setVisible(true); }
    public void chiudi() { ((Stage) checkAbilita.getScene().getWindow()).close(); }
}
