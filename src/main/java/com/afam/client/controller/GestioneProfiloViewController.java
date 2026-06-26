package com.afam.client.controller;

import com.afam.client.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class GestioneProfiloViewController implements ActivePageController {

    @FXML
    void handleCreaPortfolio(ActionEvent event) { }

    @FXML
    void handleGestionePortfolio(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/GestionePortfolioView.fxml");
    }

    @FXML
    void handleGestioneContenuti(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/GestioneContenutiView.fxml");
    }
    
    @FXML
    void handleGestioneCondivisione(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/GestioneCondivisioneView.fxml");
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
