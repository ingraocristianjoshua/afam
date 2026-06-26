package com.afam.client.controller;

import com.afam.client.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class GestioneContenutiViewController implements ActivePageController {

    @FXML
    void handleCarica(ActionEvent event) { }

    @FXML
    void handleModifica(ActionEvent event) { }

    @FXML
    void handleElimina(ActionEvent event) { }

    @FXML
    void handleVisibilita(ActionEvent event) { }

    @FXML
    void handleIndietro(ActionEvent event) {
        ViewNavigator.navigate(event, "/com/afam/client/view/GestioneProfiloView.fxml");
    }

    @Override
    public void saveState() { }

    @Override
    public void restoreState() { }
}
