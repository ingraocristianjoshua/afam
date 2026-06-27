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
 * MessAnnBnd – dialog di avviso/annullamento con tema AFAM.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class MessAnnBnd {

    @FXML private Label labelMessaggio;

    private Stage stage;

    public static void create(String messaggio) {
        create(messaggio, null);
    }

    public static void create(String messaggio, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MessAnnBnd.class.getResource("/fxml/dialog/MessAvviso.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) stage.initOwner(owner);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(loader.load());
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(
                    MessAnnBnd.class.getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            MessAnnBnd ctrl = loader.getController();
            ctrl.stage = stage;
            ctrl.labelMessaggio.setText(messaggio);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onChiudi() {
        stage.close();
    }
}
