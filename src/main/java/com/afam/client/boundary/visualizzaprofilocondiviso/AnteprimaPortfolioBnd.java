package com.afam.client.boundary.visualizzaprofilocondiviso;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.gestiscicontenuti.AnteprimaContenutoBnd;
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
 * AnteprimaPortfolioBnd – visualizzazione pubblica di un portfolio (sola lettura).
 */
public class AnteprimaPortfolioBnd {

    // ── Campi ──────────────────
    @FXML private Label labelNomePortfolio;
    @FXML private Label labelStudente;
    @FXML private Label labelVisualizzazioni;
    @FXML private VBox  boxContenuti;

    private final RestClient rest = RestClient.getInstance();

    // ── Metodi ──────────────────
    public void setPortfolio(Map<String, Object> studente, Map<String, Object> portfolio) {
        labelNomePortfolio.setText((String) portfolio.getOrDefault("nome", "Portfolio"));
        labelStudente.setText(studente.get("cognome") + " " + studente.get("nome"));
        new Thread(() -> caricaContenuti(studente, portfolio), "carica-anteprima").start();
    }

    /** Imposta portfolio condiviso. */
    @SuppressWarnings("unchecked")
    public void setPortfolioCondiviso(Map<String, Object> data) {
        Map<String, Object> portfolio = (Map<String, Object>) data.get("portfolio");
        if (portfolio != null) labelNomePortfolio.setText((String) portfolio.getOrDefault("nome", "Portfolio"));
        labelStudente.setText("");
        List<Map<String, Object>> raccolte  = (List<Map<String, Object>>) data.get("raccolte");
        List<Map<String, Object>> contenuti = (List<Map<String, Object>>) data.get("contenuti");
        int vis = portfolio != null
                ? ((Number) portfolio.getOrDefault("numeroVisualizzazioni", 0)).intValue()
                : 0;
        labelVisualizzazioni.setText(vis + " visualizzazioni");
        Platform.runLater(() -> popola(raccolte, contenuti));
    }

    /** Carica contenuti. */
    @SuppressWarnings("unchecked")
    private void caricaContenuti(Map<String, Object> studente, Map<String, Object> portfolio) {
        try {
            Map<String, Object> resp = rest.get(
                    "pubblico/studenti/" + studente.get("idUtente")
                    + "/portfolio/" + portfolio.get("idPortfolio"));
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> raccolte  = (List<Map<String, Object>>) data.get("raccolte");
            List<Map<String, Object>> contenuti = (List<Map<String, Object>>) data.get("contenuti");
            Map<String, Object> pf = (Map<String, Object>) data.get("portfolio");
            int vis = pf != null ? ((Number) pf.getOrDefault("numeroVisualizzazioni", 0)).intValue() : 0;
            Platform.runLater(() -> {
                labelVisualizzazioni.setText(vis + " visualizzazioni");
                popola(raccolte, contenuti);
            });
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> MessErrBnd.create("Errore nel caricamento: " + e.getMessage()));
        }
    }

    /** Mostra le raccolte (con i loro contenuti) e i contenuti caricati separatamente. */
    private void popola(List<Map<String, Object>> raccolte, List<Map<String, Object>> contenuti) {
        boxContenuti.getChildren().clear();

        boolean nessunaRaccolta  = raccolte  == null || raccolte.isEmpty();
        boolean nessunContenuto  = contenuti == null || contenuti.isEmpty();

        if (nessunaRaccolta && nessunContenuto) {
            Label vuoto = new Label("Nessun contenuto in questo portfolio.");
            vuoto.setStyle("-fx-text-fill: #9879e0; -fx-font-size: 13px;");
            boxContenuti.getChildren().add(vuoto);
            return;
        }

        // ── Sezione Raccolte ──
        if (!nessunaRaccolta) {
            boxContenuti.getChildren().add(titoloSezione("📚  Raccolte"));
            for (Map<String, Object> r : raccolte) {
                boxContenuti.getChildren().add(creaBloccoRaccolta(r));
            }
        }

        // ── Sezione Contenuti caricati separatamente ──
        if (!nessunContenuto) {
            boxContenuti.getChildren().add(titoloSezione("📄  Contenuti"));
            for (Map<String, Object> c : contenuti) {
                boxContenuti.getChildren().add(creaRiga(c));
            }
        }
    }

    /** Titolo sezione. */
    private Label titoloSezione(String testo) {
        Label l = new Label(testo);
        l.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 8 0 2 2;");
        return l;
    }

    /** Crea blocco raccolta. */
    @SuppressWarnings("unchecked")
    private VBox creaBloccoRaccolta(Map<String, Object> raccolta) {
        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 12; -fx-padding: 12 14;");

        Label nome = new Label("🗂  " + raccolta.getOrDefault("nome", "Raccolta"));
        nome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #c4b5fd;");
        box.getChildren().add(nome);

        List<Map<String, Object>> contenuti = (List<Map<String, Object>>) raccolta.get("contenuti");
        if (contenuti == null || contenuti.isEmpty()) {
            Label vuoto = new Label("Raccolta vuota.");
            vuoto.setStyle("-fx-text-fill: #9879e0; -fx-font-size: 12px; -fx-padding: 0 0 0 4;");
            box.getChildren().add(vuoto);
        } else {
            for (Map<String, Object> c : contenuti) box.getChildren().add(creaRiga(c));
        }
        return box;
    }

    /** Crea riga. */
    private HBox creaRiga(Map<String, Object> c) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f5f3ff; -fx-background-radius: 10; -fx-padding: 10 16;");

        String tipo = (String) c.getOrDefault("tipoFile", "");
        Label icona = new Label(iconaPer(tipo));
        icona.setStyle("-fx-font-size: 20px;");

        VBox info = new VBox(2);
        Label titolo = new Label((String) c.getOrDefault("titolo", ""));
        titolo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3d1a78;");
        Label sub = new Label(tipo.toUpperCase());
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #9879e0;");
        info.getChildren().addAll(titolo, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAnt = new Button("👁  ANTEPRIMA");
        btnAnt.getStyleClass().addAll("btn-chip", "btn-chip-blue");
        btnAnt.setOnAction(e -> onAnteprima(c));

        row.getChildren().addAll(icona, info, spacer, btnAnt);
        return row;
    }

    /** Gestisce l'azione «Anteprima». */
    private void onAnteprima(Map<String, Object> c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/gestiscicontenuti/AnteprimaContenuto.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AFAM – Anteprima – " + c.getOrDefault("titolo", ""));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            AnteprimaContenutoBnd ctrl = loader.getController();
            stage.show();
            ctrl.setContenuto(c);
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire l'anteprima: " + e.getMessage());
        }
    }

    /** Icona per. */
    private String iconaPer(String tipo) {
        if (tipo == null) return "📄";
        return switch (tipo.toLowerCase()) {
            case "pdf"  -> "📄";
            case "mp3", "wav", "flac" -> "🎵";
            case "mp4", "mov", "avi"  -> "🎬";
            case "jpg", "jpeg", "png", "gif" -> "🖼️";
            case "zip", "rar" -> "📦";
            default -> "📁";
        };
    }

    /** Chiude la finestra corrente. */
    @FXML
    public void chiudi() {
        ((Stage) boxContenuti.getScene().getWindow()).close();
    }
}
