package com.afam.client.boundary.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * MessAnnBnd – dialog di avviso/annullamento con tema AFAM.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class MessAnnBnd {

    private Alert alert;

    public static void create(String messaggio) {
        new MessAnnBnd().mostra(messaggio);
    }

    public Optional<ButtonType> mostra(String messaggio) {
        alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("AFAM – Avviso");
        alert.setHeaderText("Operazione annullata");
        alert.setContentText(messaggio);
        MessErrBnd.applica(alert);
        return alert.showAndWait();
    }

    public void chiudi()  { if (alert != null) alert.close(); }
    public void destroy() { chiudi(); alert = null; }
}
