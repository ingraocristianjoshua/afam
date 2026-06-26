package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * FormModificaBnd – form modifica nome e cognome.
 * L'email è in sola lettura; il cambio email avviene tramite flusso OTP dedicato.
 * Il numero di telefono si gestisce tramite "Valida numero".
 * @author Cristian Joshua Ingrao (0780672)
 */
public class FormModificaBnd {

    @FXML private TextField campoNome;
    @FXML private TextField campoCognome;
    @FXML private TextField campoDataNascita;
    @FXML private TextField campoEmail;
    @FXML private Label     labelErrore;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        caricaDatiAttuali();
    }

    private void caricaDatiAttuali() {
        try {
            Map<String, Object> resp = rest.get("account/profilo");
            campoNome.setText((String) resp.getOrDefault("nome", ""));
            campoCognome.setText((String) resp.getOrDefault("cognome", ""));
            campoEmail.setText((String) resp.getOrDefault("email", ""));
            String dn = (String) resp.get("dataNascita");
            campoDataNascita.setText(dn != null ? dn : "");
        } catch (RestClient.RestException e) {
            mostraErrore("Impossibile caricare i dati: " + e.getMessage());
        }
    }

    public Map<String, Object> getDati() {
        Map<String, Object> d = new HashMap<>();
        d.put("nome",    campoNome.getText().trim());
        d.put("cognome", campoCognome.getText().trim());
        return d;
    }

    @FXML
    public void onSalva() {
        labelErrore.setVisible(false);
        try {
            rest.put("account/modifica", getDati());
            MessSuccessoBnd.create("Informazioni aggiornate.");
            chiudi();
        } catch (RestClient.RestException e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML
    public void onCambiaEmail() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/gestisciaccount/CambiaEmail.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AFAM – Cambia email");
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            stage.showAndWait();
            caricaDatiAttuali();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire la schermata: " + e.getMessage());
        }
    }

    @FXML public void onAnnulla() { chiudi(); }

    public void mostraErrore(String msg) { labelErrore.setText(msg); labelErrore.setVisible(true); }
    public void chiudi() { ((Stage) campoNome.getScene().getWindow()).close(); }
}
