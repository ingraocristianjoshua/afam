package com.afam.client.boundary.gestiscicondivisione;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import com.afam.utils.Constants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PannelloLinkBnd – form per la generazione di un nuovo link di condivisione.
 * Permette di scegliere il portfolio e di copiare il link generato.
 * I portfolio sono sempre pubblici, quindi non c'è scelta di visibilità del link.
 */
public class PannelloLinkBnd {

    // ── Campi ──────────────────
    @FXML private ComboBox<Map<String, Object>> comboPortfolio;
    @FXML private TextField                     fieldEmail;
    @FXML private Label                         labelLink;
    @FXML private Button                        btnCopia;

    private final RestClient rest = RestClient.getInstance();
    private String linkGenerato;

    // ── Metodi ──────────────────
    @FXML
    public void initialize() {
        comboPortfolio.setCellFactory(lv -> portfolioCell());
        comboPortfolio.setButtonCell(portfolioCell());
        btnCopia.setDisable(true);

        new Thread(this::caricaPortfolio, "carica-portfolio-link").start();
    }

    /** Carica portfolio. */
    @SuppressWarnings("unchecked")
    private void caricaPortfolio() {
        try {
            Map<String, Object> resp = rest.get("portfolio");
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> lista = (List<Map<String, Object>>) data.get("portfolios");
            Platform.runLater(() -> comboPortfolio.setItems(FXCollections.observableArrayList(lista)));
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> MessErrBnd.create("Impossibile caricare i portfolio: " + e.getMessage()));
        }
    }

    /** Gestisce l'azione «Genera». */
    @FXML
    public void onGenera() {
        Map<String, Object> sel = comboPortfolio.getValue();
        if (sel == null) { MessErrBnd.create("Seleziona un portfolio."); return; }

        Map<String, Object> body = new HashMap<>();
        body.put("idPortfolio", sel.get("idPortfolio"));
        // I portfolio sono sempre pubblici: il link di condivisione è pubblico
        body.put("visibilita",  Constants.VIS_PUBBLICO);

        String email = fieldEmail.getText().trim();
        if (!email.isEmpty()) body.put("emailDestinatario", email);

        new Thread(() -> {
            try {
                Map<String, Object> resp = rest.post("condivisione/links", body);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                String link = (String) data.get("linkUrl");
                boolean emailSent = !email.isEmpty();
                Platform.runLater(() -> {
                    linkGenerato = link;
                    labelLink.setText(link);
                    btnCopia.setDisable(false);
                    if (emailSent) MessSuccessoBnd.create("Link generato e inviato via email.");
                    else           MessSuccessoBnd.create("Link generato.");
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Generazione fallita: " + e.getMessage()));
            }
        }, "genera-link").start();
    }

    /** Gestisce l'azione «Copia Link». */
    @FXML
    public void onCopiaLink() {
        if (linkGenerato == null) return;
        ClipboardContent content = new ClipboardContent();
        content.putString(linkGenerato);
        Clipboard.getSystemClipboard().setContent(content);
        MessSuccessoBnd.create("Link copiato negli appunti.");
    }

    /** Gestisce l'azione «Chiudi». */
    @FXML
    public void onChiudi() {
        Stage stage = (Stage) comboPortfolio.getScene().getWindow();
        stage.close();
    }

    private ListCell<Map<String, Object>> portfolioCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (String) item.get("nome"));
            }
        };
    }
}
