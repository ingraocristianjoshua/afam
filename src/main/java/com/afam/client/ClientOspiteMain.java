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
 * L'id del link condiviso (dal custom URL scheme afam://ID-LINK) arriva come
 * primo argomento dell'applicazione; il secondo argomento, opzionale, è l'URI
 * del server. In assenza di argomenti si usa la system property "afam.token"
 * (compatibilità) e, se nemmeno quella è presente, si apre la ricerca studenti.
 * Quando l'id del link è presente si carica e mostra direttamente il portfolio.
 */
public class ClientOspiteMain extends Application {

    // ── Metodi ──────────────────
    @Override
    public void start(Stage primaryStage) {
        com.afam.client.util.AppIcon.applyDockIcon();
        Image icon = com.afam.client.util.AppIcon.fxIcon();
        if (icon != null) primaryStage.getIcons().add(icon);

        // 1° arg = id del link; 2° arg (opzionale) = URI del server.
        java.util.List<String> args = getParameters() != null
                ? getParameters().getRaw() : java.util.List.of();
        String token = !args.isEmpty() ? args.get(0) : System.getProperty("afam.token");
        if (args.size() > 1 && args.get(1) != null && !args.get(1).isBlank()) {
            System.setProperty("server.baseUri", args.get(1).trim());
        }

        if (token != null && !token.isBlank()) {
            mostraCaricamento(primaryStage);
            caricaPortfolioInBackground(primaryStage, token.trim());
        } else {
            apriRicercaStudenti(primaryStage);
        }
    }

    /** Mostra caricamento. */
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

    /** Carica portfolio in background. */
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
                    // Link non valido o scaduto: mostra solo un messaggio, mai la schermata di accesso
                    Platform.runLater(() -> mostraErroreLink(stage,
                            "Il link non è più valido: potrebbe essere scaduto o revocato."));
                    return;
                } catch (Exception e) {
                    // Server non ancora pronto: riprova dopo 1 secondo
                    tentativi++;
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }
            // Dopo 10 tentativi falliti il server non è raggiungibile: messaggio, non ricerca
            Platform.runLater(() -> mostraErroreLink(stage,
                    "Impossibile contattare il server AFAM. Riprova più tardi."));
        }, "carica-portfolio-ospite").start();
    }

    /**
     * Mostra una schermata di errore minimale quando si entra da un link condiviso
     * ma il portfolio non è disponibile. Non apre la ricerca/accesso: l'ospite
     * arrivato da un link deve vedere solo il portfolio condiviso o questo avviso.
     */
    private void mostraErroreLink(Stage stage, String messaggio) {
        Label titolo = new Label("⚠️  Link non disponibile");
        titolo.setStyle("-fx-font-size: 18px; -fx-text-fill: #f5f3ff; -fx-font-weight: bold;");
        Label dett = new Label(messaggio);
        dett.setStyle("-fx-font-size: 13px; -fx-text-fill: #c4b5fd;");
        dett.setWrapText(true);
        dett.setMaxWidth(420);
        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(12, titolo, dett);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        StackPane root = new StackPane(box);
        root.setStyle("-fx-background-color: #1a0533; -fx-padding: 32;");
        Scene scene = new Scene(root, 700, 540);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        stage.setTitle("AFAM – Link non disponibile");
        stage.setScene(scene);
        if (!stage.isShowing()) stage.show();
    }

    /** Mostra portfolio. */
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
            if (!stage.isShowing()) stage.show();
        } catch (Exception e) {
            mostraErroreLink(stage, "Impossibile aprire il portfolio condiviso: " + e.getMessage());
        }
    }

    /** Apri ricerca studenti. */
    private void apriRicercaStudenti(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/visualizzaprofilocondiviso/FormRicercaStudente.fxml"));
            Scene scene = new Scene(loader.load(), 700, 520);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setTitle("AFAM – Visualizza Profilo Condiviso");
            stage.setResizable(true);
            stage.setScene(scene);
            if (!stage.isShowing()) stage.show();
        } catch (Exception ignored) {}
    }

    /** Punto di ingresso dell'applicazione. */
    public static void main(String[] args) {
        launch(args);
    }
}
