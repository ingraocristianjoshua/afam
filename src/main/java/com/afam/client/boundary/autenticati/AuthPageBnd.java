package com.afam.client.boundary.autenticati;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
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
