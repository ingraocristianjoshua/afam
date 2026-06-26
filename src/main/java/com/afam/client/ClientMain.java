package com.afam.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
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

        primaryStage.setTitle("AFAM - Identità Digitale");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
