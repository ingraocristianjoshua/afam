package com.afam.client;

import com.afam.client.boundary.visualizzaprofilocondiviso.AnteprimaPortfolioBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Punto di ingresso per il Soggetto Esterno (ospite).
 * Se viene passato il parametro "afam.token" (dal custom URL scheme afam://TOKEN),
 * carica il portfolio condiviso in background e lo mostra direttamente.
 * Altrimenti apre la schermata di ricerca studenti.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class ClientOspiteMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            var logoStream = getClass().getResourceAsStream("/images/logo.png");
            if (logoStream != null) primaryStage.getIcons().add(new Image(logoStream));
        } catch (Exception ignored) {}

        String token = System.getProperty("afam.token");
        if (token != null && !token.isBlank()) {
            mostraCaricamento(primaryStage);
            caricaPortfolioInBackground(primaryStage, token);
        } else {
            apriRicercaStudenti(primaryStage);
        }
    }

    private void mostraCaricamento(Stage stage) {
        Label lbl = new Label("Caricamento portfolio…");
        lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #c4b5fd; -fx-font-weight: bold;");
        StackPane root = new StackPane(lbl);
        root.setStyle("-fx-background-color: #1a0533;");
        Scene scene = new Scene(root, 700, 540);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        stage.setTitle("AFAM – Caricamento…");
        stage.setScene(scene);
        stage.show();
    }

    @SuppressWarnings("unchecked")
    private void caricaPortfolioInBackground(Stage stage, String token) {
        new Thread(() -> {
            int tentativi = 0;
            while (tentativi < 10) {
                try {
                    RestClient rest = RestClient.getInstance();
                    Map<String, Object> resp = rest.get("pubblico/link/" + token);
                    Map<String, Object> data = (Map<String, Object>) resp.get("data");
                    Platform.runLater(() -> mostraPortfolio(stage, data));
                    return;
                } catch (RestClient.RestException e) {
                    // Token non valido o scaduto — non ritentare
                    Platform.runLater(() -> apriRicercaStudenti(stage));
                    return;
                } catch (Exception e) {
                    // Server non ancora pronto: riprova dopo 1 secondo
                    tentativi++;
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }
            // Dopo 10 tentativi falliti, apri ricerca
            Platform.runLater(() -> apriRicercaStudenti(stage));
        }, "carica-portfolio-ospite").start();
    }

    private void mostraPortfolio(Stage stage, Map<String, Object> data) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/visualizzaprofilocondiviso/AnteprimaPortfolio.fxml"));
            Scene scene = new Scene(loader.load(), 700, 540);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            AnteprimaPortfolioBnd ctrl = loader.getController();
            ctrl.setPortfolioCondiviso(data);
            stage.setTitle("AFAM – Portfolio condiviso");
            stage.setResizable(true);
            stage.setScene(scene);
        } catch (Exception e) {
            apriRicercaStudenti(stage);
        }
    }

    private void apriRicercaStudenti(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/visualizzaprofilocondiviso/FormRicercaStudente.fxml"));
            Scene scene = new Scene(loader.load(), 700, 520);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setTitle("AFAM – Esplora profili pubblici");
            stage.setResizable(true);
            stage.setScene(scene);
            if (!stage.isShowing()) stage.show();
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        launch(args);
    }
}
