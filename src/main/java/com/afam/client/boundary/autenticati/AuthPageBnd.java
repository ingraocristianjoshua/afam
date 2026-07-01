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
 * Mostra i pulsanti: Accedi, Registrati, Ospite e SPID/eIDAS (segnaposto:
 * mostra solo un avviso, l'integrazione sarà aggiunta in futuro).
 * Non contiene logica di dominio: delega tutto al server via RestClient.
 */
public class AuthPageBnd {

    // ── Campi ──────────────────
    @FXML private Label labelTitolo;
    @FXML private StackPane logoContainer;
    @FXML private Pane bgDecor;

    /** Posizioni in frazione (x,y) delle emoji: distribuite sulle fasce laterali
     *  così restano ben disposte a qualsiasi dimensione, anche a tutto schermo. */
    private static final double[][] POS_SFONDO = {
        {0.05, 0.10}, {0.11, 0.32}, {0.04, 0.55}, {0.09, 0.75}, {0.13, 0.90},
        {0.91, 0.11}, {0.94, 0.33}, {0.87, 0.55}, {0.93, 0.75}, {0.86, 0.90}
    };

    // ── Metodi ──────────────────
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

        // Sfondo responsive: riposiziona le emoji ad ogni ridimensionamento
        // (così a tutto schermo restano distribuite e non ammassate in un angolo).
        if (bgDecor != null) {
            bgDecor.widthProperty().addListener((o, a, b) -> posizionaSfondo());
            bgDecor.heightProperty().addListener((o, a, b) -> posizionaSfondo());
            javafx.application.Platform.runLater(this::posizionaSfondo);
        }
    }

    /** Dispone le emoji in percentuale rispetto alla dimensione attuale del pannello. */
    private void posizionaSfondo() {
        if (bgDecor == null) return;
        double w = bgDecor.getWidth(), h = bgDecor.getHeight();
        if (w <= 0 || h <= 0) return;
        int i = 0;
        for (Node n : bgDecor.getChildren()) {
            if (i >= POS_SFONDO.length) break;
            n.setLayoutX(POS_SFONDO[i][0] * w);
            n.setLayoutY(POS_SFONDO[i][1] * h);
            i++;
        }
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

    /** Integrazione SPID/eIDAS – funzionalità non ancora disponibile: mostra un avviso. */
    @FXML
    public void onSpid() {
        com.afam.client.boundary.dialog.MessAnnBnd.create(
                "L'accesso con SPID / eIDAS non è ancora disponibile: sarà implementato in una versione futura.");
    }

    /** Entra come ospite: naviga alla ricerca studenti senza autenticazione. */
    @FXML
    public void onEntraComeOspite() {
        apriSchermata("/fxml/visualizzaprofilocondiviso/FormRicercaStudente.fxml", "Visualizza Profilo Condiviso");
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

    /** Chiude la finestra corrente. */
    public void chiudi() {
        Stage stage = (Stage) labelTitolo.getScene().getWindow();
        stage.close();
    }
}
