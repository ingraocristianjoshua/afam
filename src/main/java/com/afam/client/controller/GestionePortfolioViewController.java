package com.afam.client.controller;

import com.afam.client.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class GestionePortfolioViewController implements ActivePageController {

    @FXML
    void handleVisualizza(ActionEvent event) { }

    @FXML
    void handleCreaRaccolta(ActionEvent event) { }

    @FXML
    void handleAggiungiContenuto(ActionEvent event) { }

    @FXML
    void handleAggiungiAllaRaccolta(ActionEvent event) { }

    @FXML
    void handleIndietro(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/GestioneProfiloView.fxml");
    }

    @Override
    public void saveState() { }

    @Override
    public void restoreState() { }
}
