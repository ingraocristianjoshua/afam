package com.afam.client.boundary.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * MessSuccessoBnd – dialog di successo con tema AFAM.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class MessSuccessoBnd {

    private Alert alert;

    public static void create(String messaggio) {
        new MessSuccessoBnd().mostra(messaggio);
    }

    public Optional<ButtonType> mostra(String messaggio) {
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("AFAM – Operazione completata");
        alert.setHeaderText("Operazione completata con successo");
        alert.setContentText(messaggio);
        MessErrBnd.applica(alert);
        return alert.showAndWait();
    }

    public void chiudi()  { if (alert != null) alert.close(); }
    public void destroy() { chiudi(); alert = null; }
}
