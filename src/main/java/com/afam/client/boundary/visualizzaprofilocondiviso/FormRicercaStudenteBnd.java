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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * FormRicercaStudenteBnd – ricerca studenti e accesso all'area pubblica.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class FormRicercaStudenteBnd {

    @FXML private TextField fieldNome;
    @FXML private VBox      boxStudenti;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        new Thread(this::caricaTutti, "carica-studenti").start();
    }

    @FXML
    public void onCerca() {
        String query = fieldNome.getText().trim();
        new Thread(() -> {
            try {
                String path = query.isEmpty() ? "pubblico/studenti"
                        : "pubblico/studenti?nome=" + java.net.URLEncoder.encode(query, "UTF-8");
                Map<String, Object> resp = rest.get(path);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> lista = (List<Map<String, Object>>) data.get("studenti");
                Platform.runLater(() -> popolaBox(lista));
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Ricerca fallita: " + e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> MessErrBnd.create("Errore: " + e.getMessage()));
            }
        }, "cerca-studenti").start();
    }

    @FXML
    public void onAccediTramiteLink() {
        apri("/fxml/visualizzaprofilocondiviso/AccediTramiteLink.fxml", "Accedi tramite link");
    }

    private void caricaTutti() {
        try {
            Map<String, Object> resp = rest.get("pubblico/studenti");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> lista = (List<Map<String, Object>>) data.get("studenti");
            Platform.runLater(() -> popolaBox(lista));
        } catch (RestClient.RestException ignored) {
            // lista vuota — il campo rimane vuoto
        }
    }

    private void popolaBox(List<Map<String, Object>> lista) {
        boxStudenti.getChildren().clear();
        if (lista == null || lista.isEmpty()) {
            Label vuoto = new Label("Nessuno studente trovato.");
            vuoto.setStyle("-fx-text-fill: #9879e0; -fx-font-size: 14px;");
            boxStudenti.getChildren().add(vuoto);
            return;
        }
        for (Map<String, Object> s : lista) {
            boxStudenti.getChildren().add(creaRiga(s));
        }
    }

    private HBox creaRiga(Map<String, Object> s) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f5f3ff; -fx-background-radius: 10; -fx-padding: 12 16;");

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 22px;");

        VBox info = new VBox(2);
        Label nome = new Label(s.getOrDefault("cognome", "") + " " + s.getOrDefault("nome", ""));
        nome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3d1a78;");
        Label email = new Label((String) s.getOrDefault("email", ""));
        email.setStyle("-fx-font-size: 12px; -fx-text-fill: #9879e0;");
        info.getChildren().addAll(nome, email);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btn = new Button("VISUALIZZA PROFILO");
        btn.getStyleClass().addAll("btn-chip", "btn-chip-purple");
        btn.setOnAction(e -> apriProfilo(s));

        row.getChildren().addAll(avatar, info, spacer, btn);
        return row;
    }

    private void apriProfilo(Map<String, Object> studente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/fxml/visualizzaprofilocondiviso/VisualizzaProfilo.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AFAM – " + studente.getOrDefault("cognome", "") + " " + studente.getOrDefault("nome", ""));
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            VisualizzaProfiloBnd ctrl = loader.getController();
            ctrl.setStudente(studente);
            stage.show();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire il profilo: " + e.getMessage());
        }
    }

    private void apri(String fxml, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.setTitle("AFAM – " + titolo);
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            stage.show();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }
}
