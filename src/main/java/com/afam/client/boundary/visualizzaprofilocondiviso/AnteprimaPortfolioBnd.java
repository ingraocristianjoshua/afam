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
 * @author Cristian Joshua Ingrao (0780672)
 */
public class AnteprimaPortfolioBnd {

    @FXML private Label labelNomePortfolio;
    @FXML private Label labelStudente;
    @FXML private Label labelVisualizzazioni;
    @FXML private VBox  boxContenuti;

    private final RestClient rest = RestClient.getInstance();

    public void setPortfolio(Map<String, Object> studente, Map<String, Object> portfolio) {
        labelNomePortfolio.setText((String) portfolio.getOrDefault("nome", "Portfolio"));
        labelStudente.setText(studente.get("cognome") + " " + studente.get("nome"));
        new Thread(() -> caricaContenuti(studente, portfolio), "carica-anteprima").start();
    }

    public void setPortfolioCondiviso(Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        Map<String, Object> portfolio = (Map<String, Object>) data.get("portfolio");
        if (portfolio != null) labelNomePortfolio.setText((String) portfolio.getOrDefault("nome", "Portfolio"));
        labelStudente.setText("");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contenuti = (List<Map<String, Object>>) data.get("contenuti");
        int vis = portfolio != null
                ? ((Number) portfolio.getOrDefault("numeroVisualizzazioni", 0)).intValue()
                : 0;
        labelVisualizzazioni.setText(vis + " visualizzazioni");
        Platform.runLater(() -> popolaBoxContenuti(contenuti));
    }

    @SuppressWarnings("unchecked")
    private void caricaContenuti(Map<String, Object> studente, Map<String, Object> portfolio) {
        try {
            Map<String, Object> resp = rest.get(
                    "pubblico/studenti/" + studente.get("idUtente")
                    + "/portfolio/" + portfolio.get("idPortfolio"));
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> contenuti = (List<Map<String, Object>>) data.get("contenuti");
            Map<String, Object> pf = (Map<String, Object>) data.get("portfolio");
            int vis = pf != null ? ((Number) pf.getOrDefault("numeroVisualizzazioni", 0)).intValue() : 0;
            Platform.runLater(() -> {
                labelVisualizzazioni.setText(vis + " visualizzazioni");
                popolaBoxContenuti(contenuti);
            });
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> MessErrBnd.create("Errore nel caricamento: " + e.getMessage()));
        }
    }

    private void popolaBoxContenuti(List<Map<String, Object>> contenuti) {
        boxContenuti.getChildren().clear();
        if (contenuti == null || contenuti.isEmpty()) {
            Label vuoto = new Label("Nessun contenuto in questo portfolio.");
            vuoto.setStyle("-fx-text-fill: #9879e0; -fx-font-size: 13px;");
            boxContenuti.getChildren().add(vuoto);
            return;
        }
        for (Map<String, Object> c : contenuti) {
            boxContenuti.getChildren().add(creaRiga(c));
        }
    }

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

    @FXML
    public void chiudi() {
        ((Stage) boxContenuti.getScene().getWindow()).close();
    }
}
