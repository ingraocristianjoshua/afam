package com.afam.client.boundary.visualizzaprofilocondiviso;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Map;

/**
 * AccediTramiteLinkBnd – form per accedere a un portfolio tramite link condiviso.
 */
public class AccediTramiteLinkBnd {

    // ── Campi ──────────────────
    @FXML private TextField fieldToken;
    @FXML private Button    btnAccedi;

    private final RestClient rest = RestClient.getInstance();

    // ── Metodi ──────────────────
    @FXML
    public void onAccedi() {
        String input = fieldToken.getText().trim();
        if (input.isEmpty()) { MessErrBnd.create("Inserisci il codice o l'URL del link."); return; }

        // Estrae l'id del link dall'URL se l'utente ha incollato l'URL completo
        String token = input.contains("/") ? input.substring(input.lastIndexOf('/') + 1) : input;

        btnAccedi.setDisable(true);
        btnAccedi.setText("Caricamento...");

        new Thread(() -> {
            try {
                Map<String, Object> resp = rest.get("pubblico/link/" + token);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.get("data");

                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                                "/fxml/visualizzaprofilocondiviso/AnteprimaPortfolio.fxml"));
                        Stage stage = new Stage();
                        stage.setTitle("AFAM – Portfolio condiviso");
                        Scene scene = new Scene(loader.load(), 720, 560);
                        scene.getStylesheets().add(
                                getClass().getResource("/css/application.css").toExternalForm());
                        stage.setScene(scene);
                        stage.setResizable(true);
                        AnteprimaPortfolioBnd ctrl = loader.getController();
                        ctrl.setPortfolioCondiviso(data);
                        stage.show();
                        chiudi();
                    } catch (Exception e) {
                        ripristinaBottone();
                        MessErrBnd.create("Errore apertura portfolio: " + e.getMessage());
                    }
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> {
                    ripristinaBottone();
                    if (e.getStatusCode() == 410) {
                        MessErrBnd.create("Il link è scaduto o revocato.");
                    } else if (e.getStatusCode() == 404) {
                        MessErrBnd.create("Link non trovato.");
                    } else {
                        MessErrBnd.create("Accesso fallito: " + e.getMessage());
                    }
                });
            }
        }, "accedi-link").start();
    }

    /** Gestisce l'azione «Annulla». */
    @FXML
    public void onAnnulla() { chiudi(); }

    /** Chiude la finestra corrente. */
    private void chiudi() {
        Stage stage = (Stage) fieldToken.getScene().getWindow();
        stage.close();
    }

    /** Ripristina bottone. */
    private void ripristinaBottone() {
        btnAccedi.setDisable(false);
        btnAccedi.setText("ACCEDI");
    }
}
