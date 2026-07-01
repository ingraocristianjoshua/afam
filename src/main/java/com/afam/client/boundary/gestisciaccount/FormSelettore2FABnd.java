package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/** FormSelettore2FABnd – abilita o disabilita l'autenticazione a due fattori.
 * Quando il 2FA è attivo si mostra solo la disattivazione (sempre possibile);
 * quando è disattivato si chiede email e numero per l'abilitazione, subordinata
 * alla loro preventiva validazione.
 */
public class FormSelettore2FABnd {

    // ── Campi ──────────────────
    @FXML private TextField campoEmail;
    @FXML private TextField campoNumero;
    @FXML private Label     labelStato;
    @FXML private Label     labelErrore;
    @FXML private VBox      boxAbilita;
    @FXML private javafx.scene.control.Button btnAttiva;
    @FXML private javafx.scene.control.Button btnDisattiva;

    private final RestClient rest = RestClient.getInstance();

    // ── Metodi ──────────────────
    @FXML public void initialize() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
        try {
            Map<String, Object> resp = rest.get("account/2fa");
            boolean attivo = Boolean.TRUE.equals(resp.get("stato2FA"));
            aggiornaVista(attivo);
        } catch (RestClient.RestException e) {
            mostraErrore("Impossibile caricare lo stato 2FA: " + e.getMessage());
        }
    }

    /** Mostra la sezione/azione coerente con lo stato corrente del 2FA. */
    private void aggiornaVista(boolean attivo) {
        labelStato.setText("2FA attualmente: " + (attivo ? "ATTIVO" : "NON ATTIVO"));
        // In abilitazione servono i campi; in disabilitazione no
        boxAbilita.setVisible(!attivo);
        boxAbilita.setManaged(!attivo);
        btnAttiva.setVisible(!attivo);
        btnAttiva.setManaged(!attivo);
        btnDisattiva.setVisible(attivo);
        btnDisattiva.setManaged(attivo);
    }

    /** Gestisce l'azione «Attiva»: invia un OTP e ne chiede l'inserimento prima di abilitare. */
    @FXML
    public void onAttiva() {
        nascondiErrore();
        try {
            // 1) il server genera e invia l'OTP (via SMS) al numero validato
            Map<String, Object> resp = rest.post("account/2fa/invia-otp", Map.of());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            String scadenza = data != null ? (String) data.get("scadenza") : null;

            // 2) chiedo all'utente il codice OTP ricevuto
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
            dialog.setTitle("AFAM – Verifica 2FA");
            dialog.setHeaderText("Attivazione autenticazione a due fattori");
            dialog.setContentText("Inserisci il codice OTP ricevuto:");
            dialog.initOwner(labelStato.getScene().getWindow());
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-pane");

            dialog.showAndWait().ifPresent(otp -> confermaAttivazione(otp.trim(), scadenza));
        } catch (RestClient.RestException e) {
            mostraErrore(e.getMessage());
        }
    }

    /** Conferma l'abilitazione del 2FA inviando l'OTP inserito dall'utente. */
    private void confermaAttivazione(String otp, String scadenza) {
        if (otp.isEmpty()) { mostraErrore("Inserisci il codice OTP per attivare il 2FA."); return; }
        Map<String, Object> d = new HashMap<>();
        d.put("email",    campoEmail.getText().trim());
        d.put("numero",   campoNumero.getText().trim());
        d.put("abilita",  true);
        d.put("otp",      otp);
        if (scadenza != null) d.put("scadenza", scadenza);
        try {
            rest.post("account/2fa/configura", d);
            MessSuccessoBnd.create("2FA abilitato.");
            chiudi();
        } catch (RestClient.RestException e) {
            mostraErrore(e.getMessage());
        }
    }

    /** Gestisce l'azione «Disattiva». */
    @FXML
    public void onDisattiva() {
        nascondiErrore();
        Map<String, Object> d = new HashMap<>();
        d.put("abilita", false);
        try {
            rest.post("account/2fa/configura", d);
            MessSuccessoBnd.create("2FA disabilitato.");
            chiudi();
        } catch (RestClient.RestException e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML public void onAnnulla() { chiudi(); }

    /** Mostra il messaggio di errore indicato. */
    public void mostraErrore(String msg) {
        labelErrore.setText(msg);
        labelErrore.setVisible(true);
        labelErrore.setManaged(true);
    }

    /** Nasconde il messaggio di errore. */
    private void nascondiErrore() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    /** Chiude la finestra corrente. */
    public void chiudi() { ((Stage) labelStato.getScene().getWindow()).close(); }
}
