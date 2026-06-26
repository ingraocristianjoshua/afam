package com.afam.client.boundary.gestiscicondivisione;

import com.afam.client.boundary.dialog.MessConfermaBnd;
import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.afam.client.boundary.visualizzaprofilocondiviso.AnteprimaPortfolioBnd;

import java.util.List;
import java.util.Map;

/**
 * VisualizzaLinkBnd – elenco dei link con azioni inline: scadenza, visibilità, revoca.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class VisualizzaLinkBnd {

    @FXML private VBox boxLinks;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        aggiorna();
    }

    private void aggiorna() {
        new Thread(this::caricaLinks, "carica-links").start();
    }

    @SuppressWarnings("unchecked")
    private void caricaLinks() {
        try {
            Map<String, Object> resp = rest.get("condivisione/links");
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> lista = (List<Map<String, Object>>) data.get("links");
            Platform.runLater(() -> {
                boxLinks.getChildren().clear();
                if (lista == null || lista.isEmpty()) {
                    Label vuoto = new Label("Nessun link generato.");
                    vuoto.setStyle("-fx-text-fill: #9879e0; -fx-font-size: 14px;");
                    boxLinks.getChildren().add(vuoto);
                    return;
                }
                for (Map<String, Object> link : lista) {
                    boxLinks.getChildren().add(creaRiga(link));
                }
            });
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> MessErrBnd.create("Impossibile caricare i link: " + e.getMessage()));
        }
    }

    private HBox creaRiga(Map<String, Object> link) {
        String linkUrl = (String) link.getOrDefault("linkUrl", "");
        String vis     = (String) link.getOrDefault("visibilita", "privato");
        String stato   = (String) link.getOrDefault("stato", "attivo");
        String scad    = (String) link.getOrDefault("scadenza", null);
        boolean attivo = "attivo".equals(stato);

        // Riga esterna
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: rgba(255,255,255," + (attivo ? "0.06" : "0.02") + ");"
                + "-fx-background-radius: 12; -fx-padding: 12 16;"
                + (attivo ? "" : " -fx-opacity: 0.5;"));

        // URL completo + copia
        HBox urlRow = new HBox(10);
        urlRow.setAlignment(Pos.CENTER_LEFT);
        Label lUrl = new Label(linkUrl.isEmpty() ? "–" : linkUrl);
        lUrl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #c4b5fd;");
        lUrl.setWrapText(false);
        lUrl.setMaxWidth(460);
        HBox.setHgrow(lUrl, Priority.ALWAYS);
        Button btnCopia = bottone("📋 COPIA", "#6c3fc5");
        btnCopia.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(linkUrl);
            Clipboard.getSystemClipboard().setContent(cc);
            btnCopia.setText("✓ COPIATO");
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> btnCopia.setText("📋 COPIA"));
            }).start();
        });
        String urlToken = (String) link.getOrDefault("urlToken", "");
        Button btnApri = bottone("👁 ANTEPRIMA", "#1d4ed8");
        btnApri.setOnAction(e -> onApriAnteprima(urlToken, btnApri));
        urlRow.getChildren().addAll(lUrl, btnCopia, btnApri);

        // Info secondarie
        String scadInfo = scad != null ? "Scade: " + scad.substring(0, 10) : "Nessuna scadenza";
        Label lInfo = new Label("Visibilità: " + vis + "  •  Stato: " + stato + "  •  " + scadInfo);
        lInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #9879e0;");

        // Pulsanti azione
        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        Button btnScad = bottone("SCADENZA",   "#b8860b");
        Button btnVis  = bottone("VISIBILITÀ", "#2d6a4f");
        Button btnRev  = bottone("REVOCA",     "#e74c3c");
        btnScad.setOnAction(e -> onScadenza(link));
        btnVis.setOnAction(e  -> onVisibilita(link, btnVis));
        btnRev.setOnAction(e  -> onRevoca(link, btnRev));
        if (!attivo) {
            btnScad.setDisable(true);
            btnVis.setDisable(true);
            btnRev.setDisable(true);
        }
        btnRow.getChildren().addAll(btnScad, btnVis, btnRev);

        card.getChildren().addAll(urlRow, lInfo, btnRow);

        HBox row = new HBox();
        row.getChildren().add(card);
        HBox.setHgrow(card, Priority.ALWAYS);
        return row;
    }

    private Button bottone(String testo, String colore) {
        Button b = new Button(testo);
        b.setStyle("-fx-background-color: " + colore + "; -fx-text-fill: white; " +
                   "-fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 8; " +
                   "-fx-padding: 6 12; -fx-cursor: hand;");
        return b;
    }

    @SuppressWarnings("unchecked")
    private void onApriAnteprima(String urlToken, Button btn) {
        btn.setDisable(true);
        btn.setText("…");
        new Thread(() -> {
            try {
                Map<String, Object> resp = rest.get("pubblico/link/" + urlToken);
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/visualizzaprofilocondiviso/AnteprimaPortfolio.fxml"));
                        Stage stage = new Stage();
                        Scene scene = new Scene(loader.load(), 700, 540);
                        scene.getStylesheets().add(
                            getClass().getResource("/css/application.css").toExternalForm());
                        AnteprimaPortfolioBnd ctrl = loader.getController();
                        ctrl.setPortfolioCondiviso(data);
                        stage.setTitle("AFAM – Anteprima portfolio condiviso");
                        stage.setScene(scene);
                        stage.show();
                    } catch (Exception ex) {
                        MessErrBnd.create("Impossibile aprire il portfolio: " + ex.getMessage());
                    } finally {
                        btn.setDisable(false);
                        btn.setText("👁 ANTEPRIMA");
                    }
                });
            } catch (RestClient.RestException ex) {
                Platform.runLater(() -> {
                    btn.setDisable(false);
                    btn.setText("👁 ANTEPRIMA");
                    MessErrBnd.create("Link non valido o scaduto: " + ex.getMessage());
                });
            }
        }, "apri-anteprima").start();
    }

    private void onScadenza(Map<String, Object> link) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/gestiscicondivisione/FormScadenza.fxml"));
            Stage owner = (Stage) boxLinks.getScene().getWindow();
            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("AFAM – Imposta scadenza");
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            FormScadenzaBnd ctrl = loader.getController();
            ctrl.setLink(link, this::aggiorna);
            stage.showAndWait();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire il form: " + e.getMessage());
        }
    }

    private void onVisibilita(Map<String, Object> link, Button btn) {
        Object idLink = link.get("idLink");
        if (idLink == null) {
            MessErrBnd.create("ID link non disponibile.");
            return;
        }
        btn.setDisable(true);
        new Thread(() -> {
            try {
                Map<String, Object> resp = rest.patch(
                        "condivisione/links/" + idLink + "/visibilita", Map.of());
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                String nuova = data != null ? (String) data.get("visibilita") : "?";
                Platform.runLater(() -> {
                    aggiorna();
                    MessSuccessoBnd.create("Visibilità aggiornata: " + nuova);
                });
            } catch (RestClient.RestException e) {
                System.err.println("[VisualizzaLink] visibilita error: " + e.getMessage());
                Platform.runLater(() -> {
                    btn.setDisable(false);
                    MessErrBnd.create("Errore visibilità: " + e.getMessage());
                });
            }
        }, "toggle-visibilita").start();
    }

    private void onRevoca(Map<String, Object> link, Button btn) {
        if (!MessConfermaBnd.create("Revocare il link? L'operazione è irreversibile.")) return;
        btn.setDisable(true);
        new Thread(() -> {
            try {
                rest.patch("condivisione/links/" + link.get("idLink") + "/revoca", Map.of());
                Platform.runLater(() -> {
                    MessSuccessoBnd.create("Link revocato.");
                    aggiorna();
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> {
                    btn.setDisable(false);
                    MessErrBnd.create("Revoca fallita: " + e.getMessage());
                });
            }
        }, "revoca-link").start();
    }

    @FXML
    public void chiudi() {
        ((Stage) boxLinks.getScene().getWindow()).close();
    }
}
