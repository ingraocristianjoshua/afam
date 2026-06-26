package com.afam.client.boundary.homepage;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Map;

/**
 * HomePageBnd – Dashboard principale dell'applicazione.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class HomePageBnd {

    @FXML private BorderPane root;

    private final RestClient rest = RestClient.getInstance();

    @FXML public void onGestisciAccount()   { apri("/fxml/gestisciaccount/GestioneAccount.fxml",       "Gestione Account"); }
    @FXML public void onGestisciPortfolio() { apri("/fxml/gestisciportfolio/GestionePortfolio.fxml",   "Gestione Portfolio"); }
    @FXML public void onGestisciContenuti() { apri("/fxml/gestiscicontenuti/GestioneContenuti.fxml",   "Gestione Contenuti"); }
    @FXML public void onGestisciLink()      { apri("/fxml/gestiscicondivisione/GestioneCondivisione.fxml", "Gestione Condivisione"); }

    @FXML
    public void onVisualizzaProfilo() {
        new Thread(() -> {
            try {
                Map<String, Object> profilo = rest.get("account/profilo");
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/fxml/visualizzaprofilocondiviso/VisualizzaProfilo.fxml"));
                        Stage stage = new Stage();
                        stage.setTitle("AFAM – Il tuo profilo pubblico");
                        stage.setScene(new Scene(loader.load()));
                        stage.getScene().getStylesheets().add(
                                getClass().getResource("/css/application.css").toExternalForm());
                        com.afam.client.boundary.visualizzaprofilocondiviso.VisualizzaProfiloBnd ctrl =
                                loader.getController();
                        ctrl.setStudente(profilo);
                        stage.show();
                    } catch (Exception e) {
                        MessErrBnd.create("Errore apertura profilo: " + e.getMessage());
                    }
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Impossibile caricare il profilo: " + e.getMessage()));
            }
        }, "carica-profilo-pubblico").start();
    }

    @FXML
    public void onLogout() {
        try {
            rest.post("account/logout", Map.of());
        } catch (RestClient.RestException ignored) {}
        rest.logout();
        chiudi();
        apri("/fxml/autenticati/AuthPage.fxml", "AFAM");
    }

    private void chiudi() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    private void apri(String path, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Stage stage = new Stage();
            stage.setTitle("AFAM – " + titolo);
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            stage.show();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire la vista: " + e.getMessage());
        }
    }
}
