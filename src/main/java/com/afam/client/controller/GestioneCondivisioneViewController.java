package com.afam.client.controller;

import com.afam.client.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class GestioneCondivisioneViewController implements ActivePageController {

    @FXML
    void handleGenera(ActionEvent event) { }

    @FXML
    void handleVisualizza(ActionEvent event) { }

    @FXML
    void handleScadenza(ActionEvent event) { }

    @FXML
    void handleVisibilita(ActionEvent event) { }

    @FXML
    void handleRevoca(ActionEvent event) { }

    @FXML
    void handleIndietro(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/GestioneProfiloView.fxml");
    }

    @Override
    public void saveState() { }

    @Override
    public void restoreState() { }
}
