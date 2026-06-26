package com.afam.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;

public class ViewNavigator {

    public static void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(ViewNavigator.class.getResource(fxmlPath));
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            DialogFactory.showErrorDialog("Errore di Navigazione", "Impossibile caricare la schermata: " + fxmlPath);
        }
    }
}
