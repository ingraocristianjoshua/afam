package com.afam.client.boundary.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Optional;

/**
 * MessErrBnd – dialog di errore con tema AFAM.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class MessErrBnd {

    private Alert alert;

    public static void create(String messaggio) {
        new MessErrBnd().mostra(messaggio);
    }

    public Optional<ButtonType> mostra(String messaggio) {
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("AFAM – Errore");
        alert.setHeaderText("Si è verificato un errore");
        alert.setContentText(messaggio);
        applica(alert);
        return alert.showAndWait();
    }

    public void chiudi()  { if (alert != null) alert.close(); }
    public void destroy() { chiudi(); alert = null; }

    static void applica(Alert a) {
        DialogPane dp = a.getDialogPane();
        dp.getStylesheets().add(
            MessErrBnd.class.getResource("/css/application.css").toExternalForm());
        dp.getStyleClass().add("dialog-pane");
    }
}
