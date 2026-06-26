package com.afam.client.boundary.gestiscicondivisione;

import com.afam.client.boundary.dialog.MessErrBnd;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * GestioneCondivisioneBnd – hub per la gestione condivisione.
 * Due azioni: visualizza i link esistenti oppure genera un nuovo link.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class GestioneCondivisioneBnd {

    @FXML
    public void onVisualizzaLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestiscicondivisione/VisualizzaLink.fxml"));
            Stage stage = nuovoStage("Visualizza link", loader.load());
            stage.showAndWait();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    @FXML
    public void onGeneraLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestiscicondivisione/PannelloLink.fxml"));
            Stage stage = nuovoStage("Genera link condivisione", loader.load());
            stage.showAndWait();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    private Stage nuovoStage(String titolo, javafx.scene.Parent root) {
        Stage stage = new Stage();
        stage.setTitle("AFAM – " + titolo);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        stage.setScene(scene);
        return stage;
    }
}
