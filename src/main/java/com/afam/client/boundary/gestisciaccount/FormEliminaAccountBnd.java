package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.util.Map;

/** FormEliminaAccountBnd – form conferma eliminazione account.
 */
public class FormEliminaAccountBnd {

    // ── Campi ──────────────────
    @FXML private PasswordField campoPassword;
    @FXML private Label         labelErrore;

    private final RestClient rest = RestClient.getInstance();

    // ── Metodi ──────────────────
    @FXML public void initialize() { labelErrore.setVisible(false); }

    public Map<String, Object> getDati() {
        return Map.of("password", campoPassword.getText());
    }

    /** Gestisce l'azione «Elimina». */
    @FXML
    public void onElimina() {
        labelErrore.setVisible(false);
        try {
            rest.delete("account", getDati());
            rest.logout();
            MessSuccessoBnd.create("Account eliminato. Arrivederci.");
            chiudi();
            // torna alla schermata di benvenuto
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/autenticati/AuthPage.fxml"));
            Stage s = new Stage();
            s.setTitle("AFAM");
            s.setScene(new Scene(loader.load()));
            s.show();
        } catch (RestClient.RestException e) {
            mostraErrore(e.getMessage());
        } catch (Exception e) {
            mostraErrore("Errore: " + e.getMessage());
        }
    }

    @FXML public void onAnnulla() { chiudi(); }

    /** Mostra il messaggio di errore indicato. */
    public void mostraErrore(String msg) { labelErrore.setText(msg); labelErrore.setVisible(true); }
    /** Chiude la finestra corrente. */
    public void chiudi() { ((Stage) campoPassword.getScene().getWindow()).close(); }
}
