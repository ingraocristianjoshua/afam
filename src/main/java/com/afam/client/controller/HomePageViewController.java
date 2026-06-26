package com.afam.client.controller;

import com.afam.client.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class HomePageViewController implements ActivePageController {

    @FXML
    void handleGestioneAccount(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/GestioneAccountView.fxml");
    }

    @FXML
    void handleGestioneProfilo(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/GestioneProfiloView.fxml");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/AuthPageView.fxml");
    }

    @Override
    public void saveState() {
    }

    @Override
    public void restoreState() {
    }
}
