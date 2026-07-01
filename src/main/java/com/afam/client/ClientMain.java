package com.afam.client;

import com.afam.client.util.AppIcon;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

/**
 * ClientMain – punto di ingresso del client JavaFX (utente registrato).
 * Carica la schermata di accesso, applica tema e icona dell'app.
 */
public class ClientMain extends Application {

    // ── Metodi ──────────────────
    /** Avvia la UI: imposta icona, carica AuthPage.fxml e applica il CSS. */
    @Override
    public void start(Stage primaryStage) throws Exception {
        AppIcon.applyDockIcon();
        URL resource = getClass().getResource("/fxml/autenticati/AuthPage.fxml");
        if (resource == null) {
            throw new IllegalStateException("Cannot find AuthPage.fxml. Make sure it is in src/main/resources/fxml/autenticati/");
        }
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root, 800, 600);
        
        URL cssResource = getClass().getResource("/css/application.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }

        Image icon = AppIcon.fxIcon();
        if (icon != null) primaryStage.getIcons().add(icon);

        primaryStage.setTitle("AFAM - Identità Digitale");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** Bootstrap dell'applicazione JavaFX. */
    public static void main(String[] args) {
        launch(args);
    }
}
