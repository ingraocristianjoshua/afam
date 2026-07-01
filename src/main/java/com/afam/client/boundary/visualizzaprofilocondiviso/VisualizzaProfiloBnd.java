package com.afam.client.boundary.visualizzaprofilocondiviso;

import com.afam.client.boundary.dialog.MessErrBnd;
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
 * VisualizzaProfiloBnd – profilo pubblico dello studente con lista portfolio.
 * Ogni riga ha nome+email e pulsante verde VISUALIZZA PROFILO, come da mockup.
 */
public class VisualizzaProfiloBnd {

    // ── Campi ──────────────────
    @FXML private Label labelNome;
    @FXML private Label labelEmail;
    @FXML private VBox  boxPortfolio;

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> studente;

    // ── Metodi ──────────────────
    public void setStudente(Map<String, Object> s) {
        this.studente = s;
        String nome = s.getOrDefault("cognome", "") + " " + s.getOrDefault("nome", "");
        labelNome.setText(nome.trim());
        labelEmail.setText((String) s.getOrDefault("email", ""));
        new Thread(this::caricaPortfolio, "carica-portfolio-pubblico").start();
    }

    /** Carica portfolio. */
    @SuppressWarnings("unchecked")
    private void caricaPortfolio() {
        if (studente == null) return;
        try {
            Map<String, Object> resp = rest.get(
                    "pubblico/studenti/" + studente.get("idUtente") + "/profilo");
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> lista = (List<Map<String, Object>>) data.get("portfolios");
            Platform.runLater(() -> {
                boxPortfolio.getChildren().removeIf(n -> n instanceof HBox);
                if (lista == null || lista.isEmpty()) {
                    Label vuoto = new Label("Nessun portfolio pubblicato.");
                    vuoto.setStyle("-fx-text-fill: #9879e0; -fx-font-size: 13px;");
                    boxPortfolio.getChildren().add(vuoto);
                    return;
                }
                for (Map<String, Object> p : lista) {
                    boxPortfolio.getChildren().add(creaRiga(p));
                }
            });
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> MessErrBnd.create("Impossibile caricare i portfolio: " + e.getMessage()));
        }
    }

    /** Crea riga. */
    private HBox creaRiga(Map<String, Object> p) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f5f3ff; -fx-background-radius: 10; -fx-padding: 10 14;");

        VBox info = new VBox(2);
        Label nome = new Label((String) p.getOrDefault("nome", ""));
        nome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3d1a78;");
        Label vis = new Label(p.get("numeroVisualizzazioni") + " visualizzazioni");
        vis.setStyle("-fx-font-size: 11px; -fx-text-fill: #9879e0;");
        info.getChildren().addAll(nome, vis);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btn = new Button("VISUALIZZA PORTFOLIO");
        btn.getStyleClass().addAll("btn-chip", "btn-chip-green");
        btn.setOnAction(e -> apriPortfolio(p));

        row.getChildren().addAll(info, spacer, btn);
        return row;
    }

    /** Apri portfolio. */
    private void apriPortfolio(Map<String, Object> p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/fxml/visualizzaprofilocondiviso/AnteprimaPortfolio.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AFAM – " + p.get("nome"));
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            AnteprimaPortfolioBnd ctrl = loader.getController();
            ctrl.setPortfolio(studente, p);
            stage.show();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    /** Gestisce l'azione «Cerca Studenti». */
    @FXML
    public void onCercaStudenti() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/fxml/visualizzaprofilocondiviso/FormRicercaStudente.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AFAM – Ricerca studenti");
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            stage.show();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire la ricerca: " + e.getMessage());
        }
    }

    /** Chiude la finestra corrente. */
    @FXML
    public void chiudi() {
        Stage stage = (Stage) boxPortfolio.getScene().getWindow();
        stage.close();
    }
}
