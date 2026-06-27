package com.afam.client.boundary.gestiscicontenuti;

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
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * GestioneContenutiBnd – schermata con righe inline per ogni contenuto.
 * Ogni riga mostra icona, titolo/tipo e pulsanti MODIFICA/VISIBILITÀ/ELIMINA.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class GestioneContenutiBnd {

    @FXML private VBox boxContenuti;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        caricaContenuti();
    }

    @SuppressWarnings("unchecked")
    private void caricaContenuti() {
        new Thread(() -> {
        try {
            Map<String, Object> resp = rest.get("contenuti");
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> lista = (List<Map<String, Object>>) data.get("contenuti");
            Platform.runLater(() -> {
                boxContenuti.getChildren().clear();
                if (lista == null || lista.isEmpty()) {
                    Label vuoto = new Label("Nessun contenuto caricato. Clicca su CARICA CONTENUTO per iniziare.");
                    vuoto.setStyle("-fx-text-fill: #9879e0; -fx-font-size: 14px;");
                    vuoto.setWrapText(true);
                    boxContenuti.getChildren().add(vuoto);
                    return;
                }
                for (Map<String, Object> c : lista) {
                    boxContenuti.getChildren().add(creaRiga(c));
                }
            });
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> MessErrBnd.create("Impossibile caricare i contenuti: " + e.getMessage()));
        }
        }, "carica-contenuti").start();
    }

    private HBox creaRiga(Map<String, Object> c) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f5f3ff; -fx-background-radius: 10; -fx-padding: 10 14;");

        String tipo = (String) c.getOrDefault("tipoFile", "");
        Label icona = new Label(iconaPerTipo(tipo));
        icona.setStyle("-fx-font-size: 22px;");

        VBox info = new VBox(2);
        Label titolo = new Label((String) c.getOrDefault("titolo", ""));
        titolo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3d1a78;");
        Label sottotitolo = new Label(tipo + (c.get("dimensione") != null ? " · " + formatDim((Number) c.get("dimensione")) : ""));
        sottotitolo.setStyle("-fx-font-size: 11px; -fx-text-fill: #9879e0;");
        info.getChildren().addAll(titolo, sottotitolo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnMod  = bottone("MODIFICA METADATI CONTENUTO",   "btn-chip-gold");
        Button btnVis  = bottone("IMPOSTA VISIBILITÀ CONTENUTO",  "btn-chip-teal");
        Button btnElim = bottone("ELIMINA CONTENUTO",             "btn-chip-red");

        btnMod.setOnAction(e  -> onModificaContenuto(c));
        btnVis.setOnAction(e  -> onCambiaVisibilita(c));
        btnElim.setOnAction(e -> onEliminaContenuto(c));

        row.getChildren().addAll(icona, info, spacer, btnMod, btnVis, btnElim);
        return row;
    }

    private String iconaPerTipo(String tipo) {
        if (tipo == null) return "📄";
        return switch (tipo.toLowerCase()) {
            case "video", "mp4", "avi", "mov" -> "📹";
            case "audio", "mp3", "wav", "flac" -> "🎵";
            default -> "📄";
        };
    }

    private String formatDim(Number bytes) {
        if (bytes == null) return "";
        long b = bytes.longValue();
        if (b < 1024) return b + " B";
        if (b < 1024 * 1024) return (b / 1024) + " KB";
        return (b / (1024 * 1024)) + " MB";
    }

    private Button bottone(String testo, String classeColore) {
        Button b = new Button(testo);
        b.getStyleClass().addAll("btn-chip", classeColore);
        return b;
    }

    // ── Azioni ────────────────────────────────────────────────────────────────

    @FXML
    public void onCaricaContenuto() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestiscicontenuti/CompilaForm.fxml"));
            Stage stage = nuovoStage("Carica contenuto", loader.load());
            stage.showAndWait();
            caricaContenuti();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    private void onModificaContenuto(Map<String, Object> c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestiscicontenuti/FormModifiche.fxml"));
            Stage stage = nuovoStage("Modifica contenuto", loader.load());
            FormModificheBnd ctrl = loader.getController();
            ctrl.setContenuto(c);
            stage.showAndWait();
            caricaContenuti();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    private void onEliminaContenuto(Map<String, Object> c) {
        if (!MessConfermaBnd.create("Eliminare il contenuto \"" + c.get("titolo") + "\"?")) return;
        new Thread(() -> {
            try {
                rest.delete("contenuti/" + c.get("idContenuto"));
                Platform.runLater(() -> {
                    MessSuccessoBnd.create("Contenuto eliminato con successo.");
                    caricaContenuti();
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Eliminazione fallita: " + e.getMessage()));
            }
        }, "elimina-contenuto").start();
    }

    private void onCambiaVisibilita(Map<String, Object> c) {
        Object idContenuto = c.get("idContenuto");
        if (idContenuto == null) { MessErrBnd.create("ID contenuto non disponibile."); return; }
        new Thread(() -> {
            try {
                Map<String, Object> resp = rest.patch("contenuti/" + idContenuto + "/visibilita", Map.of());
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                String nuova = data != null ? (String) data.get("visibilita") : "?";
                Platform.runLater(() -> {
                    caricaContenuti();
                    MessSuccessoBnd.create("Visibilità aggiornata: " + nuova);
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Errore visibilità: " + e.getMessage()));
            }
        }, "cambia-visibilita-contenuto").start();
    }

    @FXML
    public void chiudi() {
        Stage stage = (Stage) boxContenuti.getScene().getWindow();
        stage.close();
    }

    private Stage nuovoStage(String titolo, javafx.scene.Parent root) {
        Stage stage = new Stage();
        stage.setTitle("AFAM – " + titolo);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        stage.setScene(scene);
        return stage;
    }
}
