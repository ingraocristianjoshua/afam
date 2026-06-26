package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
 * @author Cristian Joshua Ingrao (0780672)
 */
public class FormModificaBnd {

    @FXML private TextField campoNome;
    @FXML private TextField campoCognome;
    @FXML private TextField campoDataNascita;
    @FXML private TextField campoEmail;
    @FXML private Label     labelErrore;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
        new Thread(this::caricaDatiAttuali, "carica-profilo-modifica").start();
    }

    private void caricaDatiAttuali() {
        try {
            Map<String, Object> resp = rest.get("account/profilo");
            String nome    = (String) resp.getOrDefault("nome", "");
            String cognome = (String) resp.getOrDefault("cognome", "");
            String email   = (String) resp.getOrDefault("email", "");
            String dn      = formatData((String) resp.get("dataNascita"));
            Platform.runLater(() -> {
                campoNome.setText(nome);
                campoCognome.setText(cognome);
                campoEmail.setText(email);
                campoDataNascita.setText(dn);
                campoDataNascita.setPromptText("Non impostata");
            });
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> mostraErrore("Impossibile caricare i dati: " + e.getMessage()));
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
        labelErrore.setManaged(false);
        Map<String, Object> dati = getDati();

        new Thread(() -> {
            try {
                rest.put("account/modifica", dati);
                Platform.runLater(() -> {
                    MessSuccessoBnd.create("Informazioni aggiornate.");
                    chiudi();
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> mostraErrore(e.getMessage()));
            }
        }, "salva-modifica").start();
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
            new Thread(this::caricaDatiAttuali, "ricarica-profilo-modifica").start();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire la schermata: " + e.getMessage());
        }
    }

    @FXML public void onAnnulla() { chiudi(); }

    private String formatData(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try { return LocalDate.parse(iso).format(FMT); }
        catch (Exception e) { return iso; }
    }

    public void mostraErrore(String msg) {
        labelErrore.setText(msg);
        labelErrore.setVisible(true);
        labelErrore.setManaged(true);
    }

    public void chiudi() { ((Stage) campoNome.getScene().getWindow()).close(); }
}
