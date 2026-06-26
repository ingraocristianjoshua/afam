package com.afam.client.controller;

import com.afam.client.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class AuthPageViewController implements ActivePageController {

    @FXML
    void handleAccedi(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/HomePageView.fxml");
    }

    @FXML
    void handleCreaAccount(ActionEvent event) {
        System.out.println("Crea Account cliccato");
    }

    @FXML
    void handleOspite(ActionEvent event) {
        System.out.println("Entra come Ospite cliccato");
    }

    @FXML
    void handleRecuperaPassword(ActionEvent event) {
        System.out.println("Recupera Password cliccato");
    }

    @FXML
    void handleSpid(ActionEvent event) {
        System.out.println("Accedi con SPID cliccato");
    }

    @Override
    public void saveState() {
    }

    @Override
    public void restoreState() {
    }
}
