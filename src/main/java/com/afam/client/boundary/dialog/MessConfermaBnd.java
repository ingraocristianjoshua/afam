package com.afam.client.boundary.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * MessConfermaBnd – dialog di conferma (OK / Annulla) con tema AFAM.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class MessConfermaBnd {

    private Alert alert;

    public static boolean create(String domanda) {
        return new MessConfermaBnd().mostra(domanda)
                .filter(bt -> bt == ButtonType.OK)
                .isPresent();
    }

    public Optional<ButtonType> mostra(String domanda) {
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("AFAM – Conferma");
        alert.setHeaderText("Conferma operazione");
        alert.setContentText(domanda);
        MessErrBnd.applica(alert);
        return alert.showAndWait();
    }

    public void chiudi()  { if (alert != null) alert.close(); }
    public void destroy() { chiudi(); alert = null; }
}
