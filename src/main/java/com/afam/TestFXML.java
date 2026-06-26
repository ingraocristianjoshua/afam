package com.afam;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestFXML extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestisciportfolio/GestionePortfolio.fxml"));
            loader.load();
            System.out.println("Loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
