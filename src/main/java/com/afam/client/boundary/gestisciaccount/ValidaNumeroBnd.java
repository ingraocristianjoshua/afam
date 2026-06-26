package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Map;

/** ValidaNumeroBnd – invia OTP via SMS e verifica il codice.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class ValidaNumeroBnd {

    @FXML private Label     labelInfo;
    @FXML private TextField campoOTP;
    @FXML private Label     labelErrore;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        richiediOTP();
    }

    private void richiediOTP() {
        try {
            Map<String, Object> resp = rest.post("account/valida-numero/richiedi", Map.of());
            if (Boolean.TRUE.equals(resp.get("giaValidato"))) {
                labelInfo.setText("Il tuo numero è già validato.");
                return;
            }
            labelInfo.setText("Codice OTP generato. Consultare il terminale del server per il codice.");
        } catch (RestClient.RestException e) {
            mostraErrore("Errore invio OTP: " + e.getMessage());
        }
    }

    public Map<String, Object> getDati() {
        return Map.of("otp", campoOTP.getText().trim());
    }

    @FXML
    public void onVerifica() {
        labelErrore.setVisible(false);
        try {
            rest.post("account/valida-numero/conferma", getDati());
            MessSuccessoBnd.create("Numero di telefono validato.");
            chiudi();
        } catch (RestClient.RestException e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML public void onInviaDiNuovo() { campoOTP.clear(); richiediOTP(); }
    @FXML public void onAnnulla() { chiudi(); }

    public void mostraErrore(String msg) { labelErrore.setText(msg); labelErrore.setVisible(true); }
    public void chiudi() { ((Stage) campoOTP.getScene().getWindow()).close(); }
}
