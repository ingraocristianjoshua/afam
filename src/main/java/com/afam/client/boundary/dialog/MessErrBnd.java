package com.afam.client.boundary.dialog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * MessErrBnd – dialog di errore con tema AFAM.
 */
public class MessErrBnd {

    // ── Campi ──────────────────
    @FXML private Label labelMessaggio;

    private Stage stage;

    // ── Metodi ──────────────────
    public static void create(String messaggio) {
        create(messaggio, null);
    }

    /** Create. */
    public static void create(String messaggio, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MessErrBnd.class.getResource("/fxml/dialog/MessErr.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) stage.initOwner(owner);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(loader.load());
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(
                    MessErrBnd.class.getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            MessErrBnd ctrl = loader.getController();
            ctrl.stage = stage;
            ctrl.labelMessaggio.setText(messaggio);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Gestisce l'azione «Chiudi». */
    @FXML
    public void onChiudi() {
        stage.close();
    }

    /** Kept for backward compatibility — no longer needed but may be referenced elsewhere. */
    static void applica(javafx.scene.control.Alert a) {
        javafx.scene.control.DialogPane dp = a.getDialogPane();
        dp.getStylesheets().add(
            MessErrBnd.class.getResource("/css/application.css").toExternalForm());
        dp.getStyleClass().add("dialog-pane");
    }
}
