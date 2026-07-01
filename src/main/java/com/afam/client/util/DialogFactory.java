package com.afam.client.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * DialogFactory – utility legacy per finestre di dialogo native JavaFX.
 * Nota: la UI usa i dialog personalizzati in boundary/dialog; questa classe
 * resta come helper generico.
 */
public class DialogFactory {

    // ── Metodi ──────────────────
    /** Mostra un dialog modale di errore. */
    public static void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /** Mostra un dialog modale informativo (operazione riuscita). */
    public static void showSuccessDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /** Mostra un dialog di conferma; ritorna true se l'utente sceglie OK. */
    public static boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /** Avviso standard quando il server non è raggiungibile. */
    public static void showConnectionErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Errore di Connessione");
        alert.setHeaderText("Connessione Assente");
        alert.setContentText("Il server non è raggiungibile. Attendi o riprova più tardi.");
        alert.showAndWait();
    }
}
