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
 * MessConfermaBnd – dialog di conferma (CONFERMA / ANNULLA) con tema AFAM.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class MessConfermaBnd {

    @FXML private Label labelMessaggio;

    private Stage stage;
    private boolean confermato = false;

    public static boolean create(String domanda) {
        return create(domanda, null);
    }

    public static boolean create(String domanda, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MessConfermaBnd.class.getResource("/fxml/dialog/MessConferma.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) stage.initOwner(owner);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(loader.load());
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(
                    MessConfermaBnd.class.getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            MessConfermaBnd ctrl = loader.getController();
            ctrl.stage = stage;
            ctrl.labelMessaggio.setText(domanda);
            stage.showAndWait();
            return ctrl.confermato;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    public void onConferma() {
        confermato = true;
        stage.close();
    }

    @FXML
    public void onAnnulla() {
        confermato = false;
        stage.close();
    }
}
