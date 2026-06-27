package com.afam.client.boundary.autenticati;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * AuthPageBnd – schermata di accesso iniziale.
 * Mostra i pulsanti: Accedi, Registrati, SPID (stub), eIDAS (stub).
 * Non contiene logica di dominio: delega tutto al server via RestClient.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class AuthPageBnd {

    @FXML private Label labelTitolo;
    @FXML private StackPane logoContainer;
    @FXML private Pane bgDecor;

    @FXML
    public void initialize() {
        labelTitolo.setText("Alta Formazione Artistica, Musicale e Coreutica");

        // Animazione pulsante e glow sul contenitore vettoriale (Pulse & Fade Transition)
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.4), logoContainer);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.04);
        pulse.setToY(1.04);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(2.4), logoContainer);
        fade.setFromValue(0.85);
        fade.setToValue(1.0);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        animaSfondo();
    }

    /**
     * Rende dinamiche le emoji decorative dello sfondo: ogni elemento galleggia
     * verticalmente, dondola e cambia leggermente opacità, con durate/ritardi
     * sfasati così il movimento risulta naturale e non sincronizzato.
     */
    private void animaSfondo() {
        if (bgDecor == null) return;
        int i = 0;
        for (Node n : bgDecor.getChildren()) {
            double base = 3.0 + (i % 4) * 0.6;          // durata base variata
            double ritardo = (i * 0.35) % 2.0;          // sfasamento di partenza
            boolean verso = (i % 2 == 0);

            // Galleggiamento verticale
            TranslateTransition fluttua = new TranslateTransition(Duration.seconds(base), n);
            fluttua.setByY(verso ? -16 : 16);
            fluttua.setCycleCount(Animation.INDEFINITE);
            fluttua.setAutoReverse(true);
            fluttua.setDelay(Duration.seconds(ritardo));
            fluttua.play();

            // Dondolio (rotazione attorno alla posizione)
            RotateTransition dondola = new RotateTransition(Duration.seconds(base + 1.5), n);
            dondola.setByAngle(verso ? 10 : -10);
            dondola.setCycleCount(Animation.INDEFINITE);
            dondola.setAutoReverse(true);
            dondola.setDelay(Duration.seconds(ritardo));
            dondola.play();

            // Shimmer di opacità (range fisso e discreto: il CSS -fx-opacity
            // viene applicato dopo initialize(), quindi non lo leggiamo)
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

    /** Apre il form di login standard. */
    @FXML
    public void onAccedi() {
        apriSchermata("/fxml/autenticati/AccediForm.fxml", "Accedi");
    }

    /** Apre il form di registrazione. */
    @FXML
    public void onRegistrati() {
        apriSchermata("/fxml/autenticati/RegistratiForm.fxml", "Registrati");
    }

    /** Integrazione SPID – non ancora disponibile. */
    @FXML
    public void onSpid() {
        MessSuccessoBnd.create("L'accesso con SPID/eIDAS sarà implementato in futuro.");
    }

    /** Entra come ospite: naviga alla ricerca studenti senza autenticazione. */
    @FXML
    public void onEntraComeOspite() {
        apriSchermata("/fxml/visualizzaprofilocondiviso/FormRicercaStudente.fxml", "Ricerca studente");
    }

    // ── Helper navigazione ────────────────────────────────────────────────────

    private void apriSchermata(String fxmlPath, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setTitle("AFAM – " + titolo);
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            stage.show();
            chiudi();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire la schermata: " + e.getMessage());
        }
    }

    public void chiudi() {
        Stage stage = (Stage) labelTitolo.getScene().getWindow();
        stage.close();
    }
}
