package com.afam.client.controller;

import com.afam.client.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class GestioneAccountViewController implements ActivePageController {

    @FXML
    void handleModificaInfo(ActionEvent event) { }

    @FXML
    void handleReimpostaPwd(ActionEvent event) { }

    @FXML
    void handleGestisci2FA(ActionEvent event) { }

    @FXML
    void handleValidaEmail(ActionEvent event) { }

    @FXML
    void handleValidaNumero(ActionEvent event) { }

    @FXML
    void handleEliminaAccount(ActionEvent event) { }

    @FXML
    void handleLogout(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/AuthPageView.fxml");
    }
    
    @FXML
    void handleIndietro(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/HomePageView.fxml");
    }

    @Override
    public void saveState() { }

    @Override
    public void restoreState() { }
}
