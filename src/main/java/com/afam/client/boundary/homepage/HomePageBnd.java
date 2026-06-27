package com.afam.client.boundary.homepage;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.rest.RestClient;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Map;

/**
 * HomePageBnd – Dashboard principale dell'applicazione.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class HomePageBnd {

    @FXML private BorderPane root;
    @FXML private Pane bgDecor;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        animaSfondo();
    }

    /** Anima le emoji decorative dello sfondo (galleggiamento, dondolio, shimmer). */
    private void animaSfondo() {
        if (bgDecor == null) return;
        int i = 0;
        for (Node n : bgDecor.getChildren()) {
            double base = 3.0 + (i % 4) * 0.6;
            double ritardo = (i * 0.35) % 2.0;
            boolean verso = (i % 2 == 0);

            TranslateTransition fluttua = new TranslateTransition(Duration.seconds(base), n);
            fluttua.setByY(verso ? -16 : 16);
            fluttua.setCycleCount(Animation.INDEFINITE);
            fluttua.setAutoReverse(true);
            fluttua.setDelay(Duration.seconds(ritardo));
            fluttua.play();

            RotateTransition dondola = new RotateTransition(Duration.seconds(base + 1.5), n);
            dondola.setByAngle(verso ? 10 : -10);
            dondola.setCycleCount(Animation.INDEFINITE);
            dondola.setAutoReverse(true);
            dondola.setDelay(Duration.seconds(ritardo));
            dondola.play();

            FadeTransition luccica = new FadeTransition(Duration.seconds(base + 0.8), n);
            luccica.setFromValue(0.10);
            luccica.setToValue(0.20);
            luccica.setCycleCount(Animation.INDEFINITE);
            luccica.setAutoReverse(true);
            luccica.setDelay(Duration.seconds(ritardo));
            luccica.play();

            i++;
        }
    }

    @FXML public void onGestisciAccount()   { apri("/fxml/gestisciaccount/GestioneAccount.fxml",       "Gestione Account"); }
    @FXML public void onGestisciPortfolio() { apri("/fxml/gestisciportfolio/GestionePortfolio.fxml",   "Gestione Portfolio"); }
    @FXML public void onGestisciContenuti() { apri("/fxml/gestiscicontenuti/GestioneContenuti.fxml",   "Gestione Contenuti"); }
    @FXML public void onGestisciLink()      { apri("/fxml/gestiscicondivisione/GestioneCondivisione.fxml", "Gestione Condivisione"); }

    @FXML
    public void onVisualizzaProfilo() {
        new Thread(() -> {
            try {
                Map<String, Object> profilo = rest.get("account/profilo");
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/fxml/visualizzaprofilocondiviso/VisualizzaProfilo.fxml"));
                        Stage stage = new Stage();
                        stage.setTitle("AFAM – Il tuo profilo pubblico");
                        stage.setScene(new Scene(loader.load()));
                        stage.getScene().getStylesheets().add(
                                getClass().getResource("/css/application.css").toExternalForm());
                        com.afam.client.boundary.visualizzaprofilocondiviso.VisualizzaProfiloBnd ctrl =
                                loader.getController();
                        ctrl.setStudente(profilo);
                        stage.show();
                    } catch (Exception e) {
                        MessErrBnd.create("Errore apertura profilo: " + e.getMessage());
                    }
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Impossibile caricare il profilo: " + e.getMessage()));
            }
        }, "carica-profilo-pubblico").start();
    }

    @FXML
    public void onLogout() {
        try {
            rest.post("account/logout", Map.of());
        } catch (RestClient.RestException ignored) {}
        rest.logout();
        chiudi();
        apri("/fxml/autenticati/AuthPage.fxml", "AFAM");
    }

    private void chiudi() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    private void apri(String path, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Stage stage = new Stage();
            stage.setTitle("AFAM – " + titolo);
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            stage.show();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire la vista: " + e.getMessage());
        }
    }
}
